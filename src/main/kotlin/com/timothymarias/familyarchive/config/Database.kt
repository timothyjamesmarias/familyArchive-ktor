package com.timothymarias.familyarchive.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Database")

/**
 * Holds the DataSource so Koin can provide it to JobRunr and other consumers.
 */
lateinit var appDataSource: HikariDataSource
    private set

fun Application.configureDatabase() {
    val dbUrl = environment.config.property("database.url").getString()
    val dbUser = environment.config.property("database.user").getString()
    val dbPassword = environment.config.property("database.password").getString()
    val maxPoolSize = environment.config.property("database.maxPoolSize").getString().toInt()

    appDataSource = createDataSource(dbUrl, dbUser, dbPassword, maxPoolSize)

    runMigrations(appDataSource)

    Database.connect(appDataSource)
    logger.info("Database connected successfully")
}

fun createDataSource(
    url: String,
    user: String,
    password: String,
    maxPoolSize: Int,
): HikariDataSource {
    val config = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(config)
}

private fun runMigrations(dataSource: HikariDataSource) {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()

    val result = flyway.migrate()
    logger.info("Flyway migration complete: ${result.migrationsExecuted} migrations applied")
}
