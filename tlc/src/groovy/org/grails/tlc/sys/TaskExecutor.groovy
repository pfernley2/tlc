/*
 *  Copyright 2010-2013 Paul Fernley.
 *
 *  This file is part of the Three Ledger Core (TLC) software
 *  from Paul Fernley.
 *
 *  TLC is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TLC is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TLC. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grails.tlc.sys

import org.grails.tlc.corp.QueuedTask
import org.grails.tlc.corp.QueuedTaskParam
import org.grails.tlc.corp.Task
import org.grails.tlc.corp.TaskParam
import it.sauronsoftware.cron4j.Predictor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import org.apache.log4j.Logger

class TaskExecutor extends ThreadPoolExecutor {

    public static final String QUEUE_STOPPED = 'stopped'
    public static final String QUEUE_PAUSED = 'paused'
    public static final String QUEUE_HALTED = 'halted'
    public static final String QUEUE_RUNNING = 'running'

    public static final String TASK_WAITING = 'waiting'
    public static final String TASK_RUNNING = 'running'
    public static final String TASK_FAILED = 'failed'
    public static final String TASK_CANCELLED = 'cancelled'
    public static final String TASK_ABANDONED = 'abandoned'
    public static final String TASK_COMPLETED = 'completed'

    private static final Logger log = Logger.getLogger(TaskExecutor)
    private TaskScanner scanner
    private boolean paused
    private ReentrantLock pauseLock = new ReentrantLock()
    private Condition unpaused = pauseLock.newCondition()
    private AtomicLong scanned = new AtomicLong(0L)
    private AtomicBoolean hint = new AtomicBoolean(false)
    private AtomicInteger goodCount = new AtomicInteger(0)
    private AtomicLong goodTime = new AtomicLong(0L)
    private AtomicInteger badCount = new AtomicInteger(0)
    private AtomicLong badTime = new AtomicLong(0L)
    private final Date started = new Date()

    public TaskExecutor(int size, int delay, int interval, int snooze) {
        super(size, size, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())

        // Clear any dead tasks resulting from the previous queue shutdown
        for (it in QueuedTask.findAllByCurrentStatus(TASK_RUNNING)) {
            it.currentStatus = TASK_ABANDONED
            it.completedAt = new Date()
            it.saveThis()
        }

        scanner = new TaskScanner(this, delay, interval, snooze)
        scanner.start()
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable)
        pauseLock.lock()
        try {
            while (paused) {
                unpaused.await()
            }
        } catch (InterruptedException ie) {
            thread.interrupt()
        } finally {
            pauseLock.unlock()
        }
    }

    def pause() {
        pauseLock.lock()
        try {
            paused = true
        } finally {
            pauseLock.unlock()
        }
    }

    def resume() {
        pauseLock.lock()
        try {
            paused = false
            unpaused.signalAll()
        } finally {
            pauseLock.unlock()
        }
    }

    def isPaused() {
        pauseLock.lock()
        try {
            return paused
        } finally {
            pauseLock.unlock()
        }
    }

    public void shutdown() throws SecurityException {
        scanner?.interrupt()
        super.shutdown()
    }

    def resize(int size) {
        if (size > getCorePoolSize()) {
            setMaximumPoolSize(size)
            setCorePoolSize(size)
        } else {
            setCorePoolSize(size)
            setMaximumPoolSize(size)
        }
    }

    def queueStatus() {
        if (isShutdown()) {
            return QUEUE_STOPPED
        } else if (isPaused()) {
            return QUEUE_PAUSED
        } else if (!scanner?.isAlive()) {
            return QUEUE_HALTED
        } else {
            return QUEUE_RUNNING
        }
    }

    def poolSize() {
        return getCorePoolSize()
    }

    def statistics() {
        def map = [:]
        def val = queueStatus()
        map.put('status', val)
        map.put('size', poolSize())
        map.put('active', getActiveCount())
        val = scanned.get()
        if (val) {
            map.put('scanned', new Date(val))
        }

        val = goodCount.get()
        if (val) {
            map.put('goodCount', val)
            map.put('goodTime', new Date(goodTime.get()))
        }

        val = badCount.get()
        if (val) {
            map.put('badCount', val)
            map.put('badTime', new Date(badTime.get()))
        }

        map.put('delay', scanner.delay)
        map.put('interval', scanner.interval)
        map.put('snooze', scanner.snooze)
        map.put('started', started)
        map.put('now', new Date())

        return map
    }

    def scan() {
        if (isShutdown() || isPaused()) return

        // Check for scheduled jobs to queue
        for (tsk in Task.findAllByNextScheduledRunLessThanEquals(new Date())) {
            def qd = new QueuedTask()
            qd.preferredStart = tsk.nextScheduledRun
            qd.task = tsk
            qd.user = tsk.user
            qd.scheduled = true

            def params = []
            for (pmtr in TaskParam.findAllByTask(tsk)) params << new QueuedTaskParam(param: pmtr, value: pmtr.defaultValue)
            tsk.nextScheduledRun = new Predictor(tsk.schedule).nextMatchingDate()

            Task.withTransaction {status ->
                if (tsk.saveThis()) {
                    if (qd.saveThis()) {
                        for (p in params) {
                            p.queued = qd
                            if (!p.saveThis()) {
                                status.setRollbackOnly()
                                break
                            }
                        }
                    } else {
                        status.setRollbackOnly()
                    }
                } else {
                    status.setRollbackOnly()
                }
            }
        }

        // Only worth continuing if we have threads available
        def available = getCorePoolSize() - getActiveCount()
        if (available <= 0) return

        // Execute waiting tasks, in order of preferred start, upto available threads
        def queue = []
        def execs = []
        def executable
        for (qd in QueuedTask.findAllByCurrentStatusAndPreferredStartLessThanEquals(TASK_WAITING, new Date(), [max: available, sort: 'preferredStart', order: 'asc'])) {
            executable  = null
            try {
                executable = Class.forName("org.grails.tlc.tasks.${qd.task.executable}", true, scanner.getClass().getClassLoader()).newInstance()
            } catch (Throwable t) {}

            qd.startedAt = new Date()
            if (executable) {
                executable.runId = qd.id
                qd.currentStatus = TASK_RUNNING
            } else {
                qd.currentStatus = TASK_FAILED
                qd.completedAt = qd.startedAt
                qd.completionMessage = 'Unable to instantiate the executable class'
            }

            queue << qd
            execs << executable
        }

        def saved = true
        QueuedTask.withTransaction {status ->
            for (qt in queue) {
                if (!qt.saveThis()) {
                    status.setRollbackOnly()
                    saved = false
                    break
                }
            }
        }

        if (saved) {
            for (exe in execs) {
                if (exe) execute(exe)
            }
        }

        // Record the data and time of the last scan
        scanned.set(System.currentTimeMillis())
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable)

        if (((TaskExecutable) runnable).runStatus == TASK_COMPLETED) {
            goodTime.set(System.currentTimeMillis())
            goodCount.incrementAndGet()
        } else {
            badTime.set(System.currentTimeMillis())
            badCount.incrementAndGet()
        }

        hint.set(true)  // Suggest the scanner has a look
    }
}
