grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://jasperreports.sourceforge.net/maven2/"    // For patch to iText from JasperReports
        mavenRepo "http://shortrip.org/maven/"                      // For cron4j
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes

        runtime 'mysql:mysql-connector-java:5.1.23'

        // It might be a good idea to keep this in line with the one in the mail plugin
        runtime('org.springframework:spring-test:3.1.2.RELEASE') { transitive = false }

        // JasperReports for producing our reports
        compile('net.sf.jasperreports:jasperreports:5.0.1')

        // Cron facilities
        compile('it.sauronsoftware.cron4j:cron4j:2.2.5')
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ':jquery:1.8.3'
        runtime ':resources:1.2.RC2'
        runtime ':lesscss-resources:1.3.1'

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.5"

        build ":tomcat:$grailsVersion"

        // TODO: Uncomment if you wish to use database migration facilities
        //runtime ':database-migration:1.2.2'

        // TODO: TLC uses its own 'application data' caching system rather than the SpringCache system.
        //compile ':cache:1.0.1'

        compile ':mail:1.0.1'
        compile ':jquery-ui:1.8.24'
    }
}
