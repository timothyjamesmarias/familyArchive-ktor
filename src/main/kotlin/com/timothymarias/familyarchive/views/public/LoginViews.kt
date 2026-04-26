package com.timothymarias.familyarchive.views.public

import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.views.renderHtml
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.hiddenInput
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.span
import com.timothymarias.familyarchive.views.CsrfInfo

object LoginViews {
    fun login(
        ctx: ViewContext,
        csrf: CsrfInfo,
        errorMessage: String?,
    ): String =
        renderHtml {
            pageLayout(
                title = "Login - Family Archive",
                isDevMode = ctx.isDevMode,
                auth = null, // Login page doesn't need auth
            ) {
                div(classes = "max-w-md mx-auto") {
                    div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow-lg p-8") {
                        h1(classes = "text-3xl font-bold text-gray-900 dark:text-white mb-6 text-center") {
                            +"Admin Login"
                        }

                        // Error Message
                        errorMessage?.let { msg ->
                            div(
                                classes = "mb-4 bg-red-100 dark:bg-red-900 border border-red-400 dark:border-red-700 text-red-700 dark:text-red-200 px-4 py-3 rounded",
                            ) {
                                span {
                                    +msg
                                }
                            }
                        }

                        // Login Form
                        form(action = "/login", method = FormMethod.post) {
                            hiddenInput(name = csrf.parameterName) {
                                value = csrf.token
                            }

                            div(classes = "mb-4") {
                                label {
                                    htmlFor = "username"
                                    classes =
                                        setOf("block", "text-gray-700", "dark:text-gray-300", "font-medium", "mb-2")
                                    +"Email"
                                }
                                input(type = InputType.email, name = "username") {
                                    id = "username"
                                    required = true
                                    classes =
                                        setOf(
                                            "w-full",
                                            "px-4",
                                            "py-2",
                                            "border",
                                            "border-gray-300",
                                            "dark:border-gray-600",
                                            "bg-white",
                                            "dark:bg-gray-700",
                                            "text-gray-900",
                                            "dark:text-white",
                                            "rounded-lg",
                                            "focus:outline-none",
                                            "focus:ring-2",
                                            "focus:ring-blue-500",
                                            "dark:focus:ring-blue-400",
                                        )
                                    placeholder = "admin@example.com"
                                }
                            }

                            div(classes = "mb-6") {
                                label {
                                    htmlFor = "password"
                                    classes =
                                        setOf("block", "text-gray-700", "dark:text-gray-300", "font-medium", "mb-2")
                                    +"Password"
                                }
                                input(type = InputType.password, name = "password") {
                                    id = "password"
                                    required = true
                                    classes =
                                        setOf(
                                            "w-full",
                                            "px-4",
                                            "py-2",
                                            "border",
                                            "border-gray-300",
                                            "dark:border-gray-600",
                                            "bg-white",
                                            "dark:bg-gray-700",
                                            "text-gray-900",
                                            "dark:text-white",
                                            "rounded-lg",
                                            "focus:outline-none",
                                            "focus:ring-2",
                                            "focus:ring-blue-500",
                                            "dark:focus:ring-blue-400",
                                        )
                                    placeholder = "••••••••"
                                }
                            }

                            button(type = ButtonType.submit, classes = "w-full btn-primary") {
                                +"Sign In"
                            }
                        }

                        div(classes = "mt-6 text-center") {
                            a(
                                href = "/",
                                classes = "text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300",
                            ) {
                                +"← Back to Home"
                            }
                        }
                    }
                }
            }
        }
}
