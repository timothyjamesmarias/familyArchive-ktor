package com.timothymarias.familyarchive.config

import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingCall
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

/**
 * Set a flash message that will be available on the next request, then redirect.
 */
suspend fun RoutingCall.redirectWithFlash(url: String, key: String, message: String) {
    val session = sessions.get<UserSession>()
    if (session != null) {
        sessions.set(session.copy(flashMessages = session.flashMessages + (key to message)))
    }
    respondRedirect(url)
}

/**
 * Get and clear flash messages from the session.
 */
fun RoutingCall.consumeFlashMessages(): Map<String, String> {
    val session = sessions.get<UserSession>() ?: return emptyMap()
    val messages = session.flashMessages
    if (messages.isNotEmpty()) {
        sessions.set(session.copy(flashMessages = emptyMap()))
    }
    return messages
}
