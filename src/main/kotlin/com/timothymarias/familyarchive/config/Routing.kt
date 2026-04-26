package com.timothymarias.familyarchive.config

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        // Static resources
        staticResources("/dist", "static/dist")

        // Health check
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }

        // Routes will be added in Phase 5
    }
}
