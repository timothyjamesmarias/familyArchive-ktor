package com.timothymarias.familyarchive.views

import com.timothymarias.familyarchive.config.UserSession
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.routing.RoutingCall
import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.appendHTML

/**
 * Authentication info for views — replaces Spring Security's Authentication.
 */
data class AuthInfo(
    val userId: Long,
    val email: String,
    val name: String,
    val isAuthenticated: Boolean = true,
)

/**
 * CSRF token info for forms — replaces Spring Security's CsrfToken.
 */
data class CsrfInfo(
    val token: String,
    val parameterName: String = "_csrf",
    val headerName: String = "X-CSRF-Token",
)

/**
 * Context for public pages.
 */
data class ViewContext(
    val isDevMode: Boolean,
    val auth: AuthInfo? = null,
)

/**
 * Context for admin pages.
 */
data class AdminViewContext(
    val isDevMode: Boolean,
    val auth: AuthInfo? = null,
    val csrf: CsrfInfo,
    val tinymceApiKey: String,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

/**
 * Render kotlinx-html to a string — replaces HtmlMessageConverter.
 */
fun renderHtml(block: HTML.() -> Unit): String {
    val sb = StringBuilder("<!DOCTYPE html>\n")
    sb.appendHTML(prettyPrint = false).html { block() }
    return sb.toString()
}

/**
 * Build a ViewContext from the current call.
 */
fun RoutingCall.viewContext(isDevMode: Boolean): ViewContext {
    val session = sessions.get<UserSession>()
    return ViewContext(
        isDevMode = isDevMode,
        auth = session?.let { AuthInfo(it.userId, it.email, it.name) },
    )
}

/**
 * Build an AdminViewContext from the current call, consuming flash messages.
 */
fun RoutingCall.adminViewContext(isDevMode: Boolean, tinymceApiKey: String): AdminViewContext {
    val session = sessions.get<UserSession>()
        ?: throw IllegalStateException("Admin context requires an authenticated session")

    val flashMessages = session.flashMessages

    // Clear flash messages after reading
    if (flashMessages.isNotEmpty()) {
        sessions.set(session.copy(flashMessages = emptyMap()))
    }

    return AdminViewContext(
        isDevMode = isDevMode,
        auth = AuthInfo(session.userId, session.email, session.name),
        csrf = CsrfInfo(token = session.csrfToken),
        tinymceApiKey = tinymceApiKey,
        successMessage = flashMessages["success"],
        errorMessage = flashMessages["error"],
    )
}
