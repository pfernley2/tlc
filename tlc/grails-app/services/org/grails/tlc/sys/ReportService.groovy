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

import java.util.concurrent.atomic.AtomicLong
import net.sf.jasperreports.engine.JRParameter
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer
import net.sf.jasperreports.engine.util.JRConcurrentSwapFile

class ReportService {

    private static File reportDir
    private static File tempDir
    private static JRSwapFileVirtualizer virtualizer
    private static final AtomicLong nextReportNumber = new AtomicLong(((long) ((long) (System.currentTimeMillis() / 1000L)) % 308915776L))

    static transactional = false

    // Injected resources
    def sessionFactory
    def grailsApplication

    // The report parameter should be a string such as 'ChartOfAccount' representing a compiled jasper report in the
    // reports directory. This method will return a File which is the output file. HOWEVER, if the parameters contain
    // a NO_FILE_IF_NO_PAGES key with a value of true, then null will be returned if the report hase no pages.
    def createReportPDF(company, user, locale, currency, report, parameters = null) {

        // Ensure the reporting system has been initialized
        prepare()

        if (parameters == null) parameters = [:]
        parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer)
        parameters.put(JRParameter.REPORT_LOCALE, locale)
        parameters.put('SUBREPORT_DIR', reportDir.path + File.separator)
        parameters.companyName = company.name
        parameters.companyId = company.id
        parameters.companySecurityCode = company.securityCode
        parameters.currencyDecimals = currency.decimals
        parameters.userName = user.name
        parameters.userId = user.id

        JasperPrint filledReport = JasperFillManager.fillReport(reportPath(report), parameters, sessionFactory.getCurrentSession().connection())

        if (parameters.NO_FILE_IF_NO_PAGES && !filledReport?.getPages()) return null
        File outputFile = outputFilePDF(report)
        JasperExportManager.exportReportToPdfFile(filledReport, outputFile.path)

        return outputFile
    }

    static stop() {
        if (virtualizer) virtualizer.cleanup()
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private prepare() {

        // If we haven't already prepared the reporting system
        if (!reportDir) {

            synchronized (nextReportNumber) {
                if (!reportDir) {

                    // Grab our servlet context
                    def context = App.servletContext

                    // Set the report and temp paths
                    reportDir = new File(context.getRealPath('/reports'))
                    tempDir = new File(context.getRealPath('/temp'))

                    // Get the swap directory and ensure it's empty
                    def dir = new File(context.getRealPath('/temp/swap'))
                    def files = dir.listFiles()
                    for (file in files) {
                        file.delete()
                    }

                    // Create the new swap file
                    def swapFile = new JRConcurrentSwapFile(dir.path, 1024, 1024)

                    // Get the number of virtualizer pages to hold in memory
                    def size = grailsApplication.config.report.memory.pages
                    if (size == null || !(size instanceof Integer) || size <= 0 || size > 1024) size = 64

                    // Create the virtualizer
                    virtualizer = new JRSwapFileVirtualizer(size, swapFile)
                }
            }
        }
    }

    private reportPath(report) {
        return new File(reportDir, report + '.jasper').path
    }

    private outputFilePDF(report) {
        def file = new File(tempDir, report + '_' + encodeNextReportNumber() + '.pdf')
        while (file.exists()) {
            file = new File(tempDir, report + '_' + encodeNextReportNumber() + '.pdf')
        }

        return file
    }

    private encodeNextReportNumber() {
        long val = nextReportNumber.getAndIncrement()
        long radix = 26L
        String chars = 'abcdefghijklmnopqrstuvwxyz'
        StringBuilder sb = new StringBuilder()
        while (val > 0L) {
            sb.append(chars[(int) (val % radix)])
            val = (long) (val / radix)
        }

        while (sb.length() < 6) {
            sb.append('a')
        }

        return sb.toString().reverse()
    }
}
