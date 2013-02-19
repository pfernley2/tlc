// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j = {
    environments {
        production {
            appenders {
                rollingFile name: 'logfile', file: '/var/log/tomcat7.0/tlc.log'  // TODO: Set to a directory that tomcat can write to
            }
            root {
                error 'logfile'
            }
        }
    }

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

// Email settings
grails.mail.default.from="mailer@myserver.com"	// TODO: Set this and the following email settings to your own installation
grails {
	mail {
		host = 'myserver.com'
		port = 465
		username = 'mailer@myserver.com'
		password = 'mailerPassword'
		props = ['mail.smtp.auth': 'true',
				'mail.smtp.starttls.enable': 'true',
				'mail.smtp.socketFactory.port': '465',
				'mail.smtp.socketFactory.class': 'javax.net.ssl.SSLSocketFactory',
				'mail.smtp.socketFactory.fallback': 'false']
	}
}

// Using a task called 'maintain' in company 'System', the application can execute arbitrary
// native SQL statements at predetermined intervals. NOTE that, by default, the 'maintain'
// task is set up to execute once a week but is not actually enabled (you need to set the
// next scheduled run date to enable it). These native SQL statements can be useful for
// database maintenance tasks such as updating index statistics for the database optimizer
// etc where this is not done automatically. The example list below contains a single statement
// for updating the statistics in a MySQL database. Note that any statement with a question mark
// (?) in it will be executed multiple times with the name of each database table substituted in
// turn for the question mark. Any statement beginning with '{call' and ending with '}' will be
// taken to be a call to a stored procedure (with any question mark being interpreted as an IN
// parameter to the procedure of the table name) rather than the execution of a statement.
maintenance.commands = ['analyze table ?']	// TODO: MySQL specific so change if required

// The maintenance statements containing a question mark (?) have the question mark replaced, in a
// loop, with the domain table names known to the application. The application does not know about
// many-to-many join tables, however. The following list specifies tables, like the join tables,
// which need adding to the list of known tables in the application.
maintenance.extra.tables = ['company_user_access_groups', 'company_user_roles', 'system_role_activities']

// Default cache sizes in KB (A value of zero disables caching and 1024 is the maximum)
conversion.cache.size.kb = 32
exchangeRate.cache.size.kb = 16
message.cache.size.kb = 512
setting.cache.size.kb = 8
actionActivity.cache.size.kb = 48
userActivity.cache.size.kb = 48
menuCrumb.cache.size.kb = 16
pageHelp.cache.size.kb = 32
mnemonic.cache.size.kb = 16
ranges.cache.size.kb = 32
account.cache.size.kb = 64
accessGroup.cache.size.kb = 16
userAccessGroup.cache.size.kb = 8
userAccount.cache.size.kb = 1024
userCustomer.cache.size.kb = 32
userSupplier.cache.size.kb = 32

// Task queue settings
task.queue.limit = 8	       	 // The maximum number of task threads the administrator can enable (1..128)
task.queue.size = 4              // The actual number of task threads to start off with (1..task.queue.limit)
task.queue.delay.seconds = 60    // Number of seconds to wait after start up before performing the first scan (1..1000)
task.queue.interval.seconds = 30 // Number of seconds between scan of the task queue when nothing else prompts a scan (1..1000)
task.queue.snooze.seconds = 2    // Snooze interval after running a job before performing another scan (1..100)

report.memory.pages = 64         // Number of report pages to hold in memory (should be around 4 * task.queue.limit). Max is 1024

// Bookkeeping GL account code segment delimiter (possible values are -, + , |, /, :, . with - as the default)
books.segment.delimiter = '-'

// Bookkeeping GL account code mnemonic identifier (possible values are -, + , |, /, :, . with + as the default)
books.mnemonic.identifier = '+'

// Alphabetic GL account code element case setting. True means A-Z, false means a-z. Default is true.
books.alphabetic.codes.uppercase = true

// Customer account codes are converted to upper case if true. *NO* case change if false. Default is true.
books.customer.codes.uppercase = true

// Supplier account codes are converted to upper case if true. *NO* case change if false. Default is true.
books.supplier.codes.uppercase = true
