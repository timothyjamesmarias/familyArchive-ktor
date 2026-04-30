package com.timothymarias.familyarchive.config

import com.timothymarias.familyarchive.service.UserService
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.directorySessionStorage
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

private val logger = LoggerFactory.getLogger("Auth")

@Serializable
data class UserSession(
    val userId: Long,
    val email: String,
    val name: String,
    val csrfToken: String = "",
    val flashMessages: Map<String, String> = emptyMap(),
)

/**
 * Plugin for route protection and CSRF enforcement.
 */
val AuthGuard = createRouteScopedPlugin("AuthGuard") {
    onCall { call ->
        val path = call.request.path()
        val method = call.request.httpMethod
        val session = call.sessions.get<UserSession>()

        // Route protection
        val requiresAuth = when {
            path.startsWith("/admin") -> true
            path.startsWith("/api/family-tree") && method != HttpMethod.Get -> true
            path.startsWith("/api/artifact-files") && method != HttpMethod.Get -> true
            else -> false
        }

        if (requiresAuth && session == null) {
            if (path.startsWith("/api/")) {
                call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
            } else {
                call.respondRedirect("/login")
            }
        }
    }
}

fun Application.configureAuth() {
    install(Sessions) {
        cookie<UserSession>("user_session", directorySessionStorage(File("build/.sessions"))) {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = 86400
        }
    }

    // Login and logout routes
    routing {
        post("/login") {
            val userService by inject<UserService>()
            val params = call.receiveParameters()
            val email = params["username"] ?: ""
            val password = params["password"] ?: ""

            val user = userService.findByEmail(email)
            if (user != null && userService.verifyPassword(password, user.password)) {
                val csrfToken = UUID.randomUUID().toString()
                call.sessions.set(UserSession(user.id, user.email, user.name, csrfToken))
                call.respondRedirect("/admin/dashboard")
            } else {
                call.respondRedirect("/login?error=true")
            }
        }

        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }
    }
}

/**
 * Bootstrap admin user on startup.
 */
fun Application.bootstrapAdminUser() {
    val userService by inject<UserService>()
    val isDev = developmentMode

    val adminEmail = System.getenv("ADMIN_EMAIL") ?: "admin@familyarchive.local"
    val adminName = System.getenv("ADMIN_NAME") ?: "Admin User"

    if (userService.findByEmail(adminEmail) != null) {
        logger.info("Admin user with email '{}' already exists, skipping creation", adminEmail)
        return
    }

    val adminPassword = System.getenv("ADMIN_PASSWORD")
    if (adminPassword.isNullOrBlank()) {
        if (isDev) {
            logger.info("ADMIN_PASSWORD not set in dev mode, skipping admin user creation")
            return
        } else {
            logger.error("ADMIN_PASSWORD environment variable is required in production")
            throw IllegalStateException("ADMIN_PASSWORD environment variable is required to bootstrap admin user")
        }
    }

    userService.create(adminEmail, adminPassword, adminName)
    logger.info("Admin user created successfully (email: {})", adminEmail)
}
