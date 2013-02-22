// Common dataSource settings
dataSource {
    pooled = true
    dbCreate = 'update'
    driverClassName = 'com.mysql.jdbc.Driver'	// TODO: Change this if you are not using MySQL

    // TODO: The following sets up the Apache Commons DBCP database connection pooling
    // to avoid potential connection timeout problems when used in a long-running
    // environment (such a Production system which only gets re-booted as infrequently
    // as possible). The following are assumptions and therefore you should check them
    // out. In particular, ensure that the validationQuery is a valid SQL statement
    // for your database.
    properties {
        maxActive = 25                                  // Allow 16 active 'user' connections + the task.queue.limit (default is 8) + 1 for the task scanner
        maxIdle= 12                                     // Allow about half the connections to be idle at the same time before closing the excess
        minEvictableIdleTimeMillis = 1000 * 60 * 5      // Allow a connection to be idle for 5 minutes before being eligible for eviction
        timeBetweenEvictionRunsMillis = 1000 * 60 * 5   // Run the eviction routine every 5 minutes
        numTestsPerEvictionRun = 4                      // Evict up to 4 idle connections in any eviction run
        testOnBorrow = true                             // Ensure the connection is alive before giving it to us (consider db server, firewall and O/S TCP/IP timeouts)
        testWhileIdle = false                           // Don't check connections when they're idle
        testOnReturn = false                            // Don't check a connection is alive when we give it back
        validationQuery = 'SELECT 1'                    // The SQL statement (must return at least one row) to use for testing - (Oracle would be "SELECT 1 FROM DUAL")
    }
}

// Hibernate caching settings
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}

// TODO: Change these environment specific settings to appropriate values for your installation
environments {
    development {
        dataSource {
            url = 'jdbc:mysql://localhost:3306/development'
            username = 'developer'
            password = 'developing'
        }
    }
    test {
        dataSource {
            url = 'jdbc:mysql://localhost:3306/testing'
            username = 'tester'
            password = 'testing'
        }
    }
    production {
        dataSource {
            url = 'jdbc:mysql://localhost:3306/production'
            username = 'username'
            password = 'password'
        }
    }
}
