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
import java.util.concurrent.TimeUnit

class TaskService {

    private static final lock = new Object()
    private static sizeLimit = 8
    private static size = 4
    private static delay = 60
    private static interval = 30
    private static snooze = 2
    private static executor

    static transactional = false

    static start() {
        synchronized (lock) {
            if (isAlive()) return false

            // If first time through
            if (!executor) {
                def val = App.config.task.queue.limit
                if (val != null && val instanceof Integer && val > 0 && val <= 128) {
                    sizeLimit = val
                }

                val = App.config.task.queue.size
                if (val != null && val instanceof Integer && val > 0 && val <= sizeLimit) {
                    size = val
                }

                val = App.config.task.queue.delay.seconds
                if (val != null && val instanceof Integer && val > 0 && val <= 1000) {
                    delay = val
                }

                val = App.config.task.queue.interval.seconds
                if (val != null && val instanceof Integer && val > 0 && val <= 1000) {
                    interval = val
                }

                val = App.config.task.queue.snooze.seconds
                if (val != null && val instanceof Integer && val > 0 && val <= 100) {
                    snooze = val
                }
            }

            executor = new TaskExecutor(size, delay, interval, snooze)
            return true
        }
    }

    static stop() {
        synchronized (lock) {
            if (!isAlive()) return false

            executor.shutdown() // Disable new tasks from being submitted

            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {

                executor.shutdownNow() // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                executor.awaitTermination(10, TimeUnit.SECONDS)
            }

            return true
        }
    }

    def pause() {
        synchronized (lock) {
            if (!isAlive() || executor.isPaused()) return false

            executor.pause()
            return true
        }
    }

    def resume() {
        synchronized (lock) {
            if (!isAlive() || !executor.isPaused()) return false

            executor.resume()
            return true
        }
    }

    // The parameters parameter is a list of maps where each map contains... [param: TaskParamInstance, value: StringValueOrNull]
    def submit(task, parameters, user, preferredStart = new Date()) {
        def queued = new QueuedTask()
        queued.task = task
        queued.user = user
        queued.preferredStart = preferredStart
        queued.scheduled = false
        def valid = true
        QueuedTask.withTransaction {status ->
            if (queued.saveThis()) {
                if (parameters) {
                    for (param in parameters) {
                        def p = new QueuedTaskParam()
                        p.queued = queued
                        p.param = param.param
                        p.value = param.value
                        if (!p.saveThis()) {
                            status.setRollbackOnly()
                            valid = false
                            break
                        }
                    }
                }
            } else {
                status.setRollbackOnly()
                valid = false
            }
        }

        // If we saved the queued task and its parameters ok and the preferred start date and time is 'immediately'
        if (valid && queued.preferredStart <= new Date()) {
            synchronized (lock) {

                // If the executor is alive and is not paused, give it a poke in the ribs
                // to make it scan the queue and thus find the entry we have just created.
                if (isAlive() && !executor.isPaused()) executor.hint.set(true)
            }
        }

        return valid ? queued.id : null // Return the queued task id or null if there was an error
    }

    def resize(int newSize) {
        synchronized (lock) {
            if (!isAlive() || newSize <= 0 || newSize > sizeLimit || newSize == size) return false

            executor.resize(newSize)
            size = newSize
            return true
        }
    }

    def statistics() {
        synchronized (lock) {
            return executor ? executor.statistics() : null
        }
    }

    def queueStatus() {
        synchronized (lock) {
            return executor ? executor.queueStatus() : null
        }
    }

    def poolSize() {
        synchronized (lock) {
            return executor ? executor.poolSize() : null
        }
    }

    def poolSizeLimit() {
        synchronized (lock) {
            return sizeLimit
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private static isAlive() {
        return (executor && !executor.isShutdown())
    }
}
