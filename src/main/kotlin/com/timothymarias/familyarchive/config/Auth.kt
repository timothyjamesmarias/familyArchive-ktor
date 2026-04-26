package com.timothymarias.familyarchive.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.directorySessionStorage
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class UserSession(
    val userId: Long,
    val email: String,
    val name: String,
    val csrfToken: String = "",
    val flashMessages: Map<String, String> = emptyMap(),
)

fun Application.configureAuth() {
    install(Sessions) {
        cookie<UserSession>("user_session", directorySessionStorage(File("build/.sessions"))) {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = 86400 // 24 hours
        }
    }

    // Full auth configuration will be added in Phase 4
}
