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

import org.grails.tlc.books.BookService
import org.grails.tlc.books.PostingService
import grails.plugin.mail.MailService
import grails.util.GrailsUtil
import grails.util.GrailsWebUtil
import java.nio.channels.ClosedByInterruptException
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.FlushMode
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.context.MessageSource
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.grails.tlc.corp.*

class TaskExecutable implements Runnable {

    static MessageSource runMessageSource
    static SessionFactory runSessionFactory

    // *******************************************************************
    // **** START of variables intended for direct use by sub-classes ****
    // *******************************************************************
    public static final Logger log = Logger.getLogger(TaskExecutable)    // A logger
    static GrailsApplication grailsApplication  // The application in which we are running
    static UtilService utilService              // Our util service (gives access to other services as well)
	static MailService mailService              // The mail plugin service
    static BookService bookService              // Our bookkeeping service
    static PostingService postingService        // Our posting service

    String taskCode                 // The code of the task being executed
    String taskName                 // The name of the task being executed
    Boolean taskScheduled           // Set to true if this current run was scheduled, false if it was on demand
    Date taskStart                  // The date and time that this task run started
    Company company                 // The company for which the task is being run
    ExchangeCurrency currency       // The currency of the company
    SystemUser user                 // The user under whose authority the task is being run
    Locale locale                   // The locale to use within the task
    Map params                      // The input parameters for this task
    Map results = [:]               // The output results from this task
    String completionMessage        // Any completion message from this task
    // *******************************************************************
    // **** END of variables intended for direct use by sub-classes   ****
    // *******************************************************************


    Long runId
    QueuedTask runQueued
    Map runResultStrings
    String runStatus
    def runBound

    def runPriorLoaded
    def runPriorCompleted
    def runPriorScheduled
    def runPriorQueued

    // The main processing method. It calls the 'execute' method of sub-classes
    // to actually perform the work.
    public void run() {
        try {
            runStatus = TaskExecutor.TASK_RUNNING
            bindSession()

            runQueued = QueuedTask.get(runId)
            params = loadParams(runQueued)
            runResultStrings = loadResultStrings(runQueued)
            company = runQueued.task.company
            currency = ExchangeCurrency.findByCompanyAndCompanyCurrency(company, true, [cache: true])
            user = runQueued.user
            taskCode = runQueued.task.code
            taskName = runQueued.task.name
            taskScheduled = runQueued.scheduled
            taskStart = runQueued.dateCreated
            user.refresh()  // Javassist bug workaround
            locale = new Locale(user.language.code, user.country.code)
            utilService.createDummySessionData(company, currency, user, locale)

            def result = execute()
            if (result != null && result instanceof Boolean && !result) {
                runStatus = TaskExecutor.TASK_FAILED
            } else {
                runStatus = TaskExecutor.TASK_COMPLETED
            }
        } catch (InterruptedException ie) {
            setRunFailure(TaskExecutor.TASK_CANCELLED, ie)
        } catch (ClosedByInterruptException cbie) {
            setRunFailure(TaskExecutor.TASK_CANCELLED, cbie)
        } catch (Exception ex) {
            setRunFailure(TaskExecutor.TASK_FAILED, ex)
            log.error(this.getClass().getName(), ex)
        } finally {
            try {
                storeResultStrings(runResultStrings, results)
                try {
                    QueuedTask.withTransaction {status ->
                        runQueued.refresh()
                        runQueued.currentStatus = runStatus
                        runQueued.completedAt = new Date()
                        if (completionMessage?.length() > 200) completionMessage = completionMessage.substring(0, 200)
                        runQueued.completionMessage = completionMessage
                        if (runQueued.saveThis()) {
                            for (entry in runResultStrings) {
                                def val = entry.value.value
                                if (val != null) {
                                    def rslt = new QueuedTaskResult()
                                    rslt.queued = runQueued
                                    rslt.result = TaskResult.get(entry.value.id)
                                    if (val.length() > 200) val = val.substring(0, 197) + '...'
                                    rslt.value = val
                                    if (!rslt.saveThis()) throw new IllegalArgumentException('Unable to update the queued task results')
                                }
                            }
                        } else {
                            throw new IllegalArgumentException('Unable to update the queued task completion details')
                        }
                    }
                } catch (Exception ignore) {
                    log.error(this.getClass().getName(), ignore)
                }
            } finally {
                unbindSession()
            }
        }
    }

    // This method should be overridden by descendants. The overriding method
    // should have a code structure similar to that illustrated below. Note that
    // the code does not attempt to catch exceptions but cleans up using a
    // 'finally' clause. Returning false from this method will be taken to mean
    // failure while returning true (or nothing or anything else) will be taken
    // to mean success. In either case, the method can (but does not have to)
    // set the 'completionMessage' variable to describe the outcome of the run.
    // As often as is practical, the overriding method should call the 'yield'
    // method which will abort operation (by throwing an exception) if the task
    // is to be terminated ASAP.
    def execute() {
        //  def resource
        //  try {
        //
        //      resource.open(params.resourceName)
        //      yield()
        //
        //      if (!resource.getBooleanTestValue()) {
        //          completionMessage = message(code: 'bad.test.value')
        //          return false
        //      }
        //
        //      MyDomain.withTransaction {
        //          ...put updates within a transaction and outside any select loop
        //      }
        //
        //      def reportParams = [reportTitle: 'Example Of How To Run A Report', column1: 'First Column Heading')
        //      def pdfFile = createReportPDF('ExampleReport', reportParams)    // Note our params are added automatically
        //      ...do something with the PDF output file and then delete it
        //
        //      def prior = getPriorRun(true, true)
        //      def map = getPriorResults()
        //      def val = map?.resultValue ?: 123.456
        //      val = utilService.round(val, 2)
        //      results.resultValue = val
        //      log.debug("** OUR ** result value set to ${val}")
        //
        //  } finally {
        //      if (resource) {
        //          try {
        //              resource.close()
        //          } catch (Exception ex) {}
        //      }
        //  }
    }

    // Check if this thread has been interrupted and, if so, throw an
    // InterruptedException for the 'run' method to catch.
    def yield() {
        if (Thread.interrupted()) {
            throw new InterruptedException()
        }
    }

    // Localize a message
    def message(params) {

        // Set the special parameters for them
        params.locale = locale
        params.securityCode = company.securityCode

        return runMessageSource.getMessageText(params)
    }

    def createReportPDF(report, parameters, omitTaskParameters = false) {

        // Need to add standard messages here since reportService doesn't have an active message source
        parameters.endOfReport = message(code: 'generic.report.ending', default: '--End of Report--')
        parameters.reportParameters = message(code: 'generic.report.parameters', default: 'Report Parameters')

        if (!omitTaskParameters) {

            // We also need to add in the params values and their prompts
            def tsk = Task.findByCompanyAndCode(company, taskCode)
            for (it in TaskParam.findAllByTask(tsk)) {
                if (!parameters."${it.code}") parameters.put(it.code, params."${it.code}")
                if (!parameters."${it.code}Prompt") parameters.put(it.code + 'Prompt', message(code: "taskParam.name.${tsk.code}.${it.code}", default: it.name))
            }
        }

        // Get utilService to actually run the report
        return utilService.reportService.createReportPDF(company, user, locale, currency, report, parameters)
    }

    // Returns true if there is a task of this type already running, else false.
    // If scheduled = true, only scheduled runs will be considered
    // If scheduled = false, only on-demand runs will be considered
    // If scheduled = null, any run type will be considered
    def isPreempted(scheduled = null) {
        def sql = "select count(*) from QueuedTask as x where x.task = ? and x.currentStatus in ('waiting', 'running') and x.startedAt < ?"
        def args = [runQueued.task, taskStart]

        if (scheduled != null) {
            sql += ' and x.scheduled = ?'
            args << scheduled
        }

        return (QueuedTask.executeQuery(sql, args)[0] > 0)
    }

    // If completed = true, only a completed run will be chosen
    // If completed = false, only a failed, cancelled or abandoned run will be chosen
    // If completed = null, any run completion code will be chosen
    // If scheduled = true, only a scheduled run will be chosen
    // If scheduled = false, only an on-demand run will be chosen
    // If scheduled = null, any run type will be chosen
    // Can return null if there are no matches to the conditions, otherwise
    // will return the prior run matching the given conditions. This method
    // actually only returns a map rather than the QueuedTask instance itself.
    def getPriorRun(completed = null, scheduled = null) {
        setPriorQueued(completed, scheduled)
        return runPriorQueued ? [task: runPriorQueued.task, user: runPriorQueued.user, submittedAt: runPriorQueued.dateCreated,
                scheduled: runPriorQueued.scheduled, currentStatus: runPriorQueued.currentStatus, preferredStart: runPriorQueued.preferredStart,
                startedAt: runPriorQueued.startedAt, completedAt: runPriorQueued.completedAt, completionMessage: runPriorQueued.completionMessage] : null
    }

    // Returns the parameters associated with the prior run - which must
    // already have been loaded with the getPriorRun() method. If this
    // is not the case then null will be returned. An empty map will be
    // returned if the run had no parameters.
    def getPriorParams() {
        return runPriorQueued ? loadParams(runPriorQueued) : null
    }

    // Returns the results associated with the prior run - which must
    // already have been loaded with the getPriorRun() method. If this
    // is not the case then null will be returned. An empty map will be
    // returned if the run had no results. Parameter values themselves
    // may also be null within the map.
    def getPriorResults() {
        return runPriorQueued ? loadResults(runPriorQueued) : null
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    // Used by the Spring IoC container to inject the message source bean
    public void setMessageSource(MessageSource messageSource) {
        runMessageSource = messageSource
    }

    // Used by Spring to inject the Grails application
    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }

    // Used by Spring to inject the Hibernate session factory
    public void setSessionFactory(SessionFactory sessionFactory) {
        runSessionFactory = sessionFactory
    }

    // Used by Spring to inject our utility service
    public void setUtilService(UtilService utilService) {
        this.utilService = utilService
    }
	
	// Used by Spring to inject the mail service from the mail plugin
	public void setMailService(MailService mailService) {
		this.mailService = mailService
	}

    // Used by Spring to inject our bookkeeping service
    public void setBookService(BookService bookService) {
        this.bookService = bookService
    }

    // Used by Spring to inject our posting service
    public void setPostingService(PostingService postingService) {
        this.postingService = postingService
    }

    private setRunFailure(code, ex) {
        runStatus = code
        def cause = GrailsUtil.extractRootCause(ex)
        completionMessage = cause.class.name
        if (cause.getMessage()) completionMessage += "(${cause.getMessage()})"
    }

    private bindSession() {
        def holder = TransactionSynchronizationManager.getResource(runSessionFactory)
        if (holder) {
            holder.getSession().flush()
            runBound = false
        } else {
            Session session = SessionFactoryUtils.getSession(runSessionFactory, true)
            session.setFlushMode(FlushMode.AUTO)
            TransactionSynchronizationManager.bindResource(runSessionFactory, new SessionHolder(session))
            runBound = true
        }

        // Attach a dummy web request to this thread so that we can pretend
        // to be an interactive session if necessary. This should only be
        // needed by domain validation failures and other 'behind the scenes'
        // errors whih dash off and get an error message (which needs a request).
        def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(App.servletContext)
        GrailsWebUtil.bindMockWebRequest(applicationContext)
    }

    private unbindSession() {
        if (runBound) {
            runBound = false
            try {
                Session session = ((SessionHolder) TransactionSynchronizationManager.unbindResource(runSessionFactory)).getSession()
                if (session.getFlushMode() != FlushMode.MANUAL) session.flush()
                SessionFactoryUtils.closeSession(session)
            } catch (Exception ex) {}
        }

        // Release the dummy request we created and attached to this thread
        RequestContextHolder.setRequestAttributes(null)
    }

    private setPriorQueued(completed, scheduled) {
        if (runPriorLoaded && runPriorCompleted == completed && runPriorScheduled == scheduled) {
            return
        }

        runPriorLoaded = true
        runPriorCompleted = completed
        runPriorScheduled = scheduled
        runPriorQueued = null

        def sql = 'from QueuedTask as x where x.task = ? and x.completedAt < ?'
        def args = [runQueued.task, taskStart]

        if (completed != null) {
            if (completed) {
                sql += ' and x.currentStatus = ?'
            } else {
                sql += ' and x.currentStatus != ?'
            }

            args << TaskExecutor.TASK_COMPLETED
        }

        if (scheduled != null) {
            sql += ' and x.scheduled = ?'
            args << scheduled
        }

        sql += ' order by x.completedAt desc'
        def lst = QueuedTask.findAll(sql, args, [max: 1])
        runPriorQueued = lst ? lst[0] : null
    }

    private loadParams(queued) {
        def map = [:]
        for (it in queued.parameters) {
            if (it.value) {
                def val = UtilService.valueOf(it.param.dataType, it.param.dataScale, it.value)
                if (val != null) map.put(it.param.code, val)
            }
        }

        return map
    }

    private loadResults(queued) {
        def map = [:]
        for (it in queued.results) {
            if (it.value) {
                def val = UtilService.valueOf(it.result.dataType, it.result.dataScale, it.value)
                if (val != null) map.put(it.result.code, val)
            }
        }

        return map
    }

    private loadResultStrings(queued) {
        def map = [:]
        for (it in queued.task.results) {
            map.put(it.code, [id: it.id, type: it.dataType, scale: it.dataScale])
        }

        return map
    }

    private storeResultStrings(defs, vals) {
        def val
        for (it in defs) {
            val = UtilService.stringOf(it.value.type, it.value.scale, vals.get(it.key))
            if (val != null) it.value.value = val
        }
    }
}
