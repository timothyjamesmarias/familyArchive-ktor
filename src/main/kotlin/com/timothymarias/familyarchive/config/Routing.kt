package com.timothymarias.familyarchive.config

import com.timothymarias.familyarchive.routes.adminRoutes
import com.timothymarias.familyarchive.routes.apiRoutes
import com.timothymarias.familyarchive.routes.publicRoutes
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val isDevMode = environment.config.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: false
    val tinymceApiKey = environment.config.propertyOrNull("app.tinymceApiKey")?.getString() ?: "no-api-key"

    routing {
        // Auth guard for route protection
        install(AuthGuard)

        // Static resources
        staticResources("/dist", "static/dist")

        // Health check
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }

        // Public routes
        publicRoutes(isDevMode)

        // API routes
        apiRoutes()

        // Admin routes
        adminRoutes(isDevMode, tinymceApiKey)
    }
}
