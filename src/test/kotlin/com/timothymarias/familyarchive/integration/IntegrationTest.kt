package com.timothymarias.familyarchive.integration

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Base class for integration tests.
 *
 * Starts a PostgreSQL TestContainer, configures the Ktor test app to use it,
 * and provides a configured HTTP client with JSON and cookie support.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("familyarchive_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @BeforeAll
        fun startContainer() {
            postgres.start()
            System.setProperty("TEST_DB_URL", postgres.jdbcUrl)
            System.setProperty("TEST_DB_USER", postgres.username)
            System.setProperty("TEST_DB_PASSWORD", postgres.password)
        }

        @JvmStatic
        @AfterAll
        fun stopContainer() {
            postgres.stop()
        }
    }

    @AfterEach
    fun cleanupKoin() {
        try { stopKoin() } catch (_: Exception) {}
    }

    /**
     * Run a test against the full Ktor application with TestContainers PostgreSQL.
     */
    protected fun integrationTest(
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit,
    ) = testApplication {
        environment {
            config = ApplicationConfig("application-test.conf")
        }

        val testClient = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpCookies)
        }

        block(testClient)
    }
}
