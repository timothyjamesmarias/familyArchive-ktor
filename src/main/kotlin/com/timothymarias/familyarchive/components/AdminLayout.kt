package com.timothymarias.familyarchive.components

import com.timothymarias.familyarchive.components.IconHelpers.closeIcon
import com.timothymarias.familyarchive.components.IconHelpers.hamburgerIcon
import com.timothymarias.familyarchive.components.IconHelpers.moonIcon
import com.timothymarias.familyarchive.components.IconHelpers.settingsIcon
import com.timothymarias.familyarchive.components.IconHelpers.sunIcon
import kotlinx.html.BODY
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.MAIN
import kotlinx.html.a
import kotlinx.html.aside
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.hiddenInput
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import com.timothymarias.familyarchive.views.AuthInfo
import com.timothymarias.familyarchive.views.CsrfInfo

fun HTML.adminLayout(
    title: String = "Admin - Family Archive",
    pageTitle: String = "Dashboard",
    isDevMode: Boolean,
    auth: AuthInfo?,
    csrf: CsrfInfo,
    successMessage: String? = null,
    errorMessage: String? = null,
    head: HEAD.() -> Unit = {},
    scripts: BODY.() -> Unit = {},
    content: MAIN.() -> Unit,
) {
    lang = "en"
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title(content = title)

        // Dark mode prevention script
        script {
            unsafe {
                +
                    """
                    (function() {
                        const theme = localStorage.getItem('theme');
                        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                        if (theme === 'dark' || (!theme && prefersDark)) {
                            document.documentElement.classList.add('dark');
                        }
                    })();
                    """.trimIndent()
            }
        }

        if (isDevMode) {
            script(src = "http://localhost:5173/@vite/client") {
                attributes["type"] = "module"
            }
            script(src = "http://localhost:5173/admin/js/admin.ts") {
                attributes["type"] = "module"
            }
            link(rel = "stylesheet", href = "http://localhost:5173/admin/css/admin.css")
        } else {
            link(rel = "stylesheet", href = "/dist/assets/admin.css")
            script(src = "/dist/assets/admin-app.js") {
                attributes["type"] = "module"
            }
        }

        head()
    }

    body(classes = "bg-gray-100 dark:bg-gray-900 min-h-screen transition-colors font-sans") {
        // Admin Header
        adminHeader(auth, csrf)

        div(classes = "flex relative") {
            // Sidebar
            adminSidebar()

            // Sidebar overlay (mobile only)
            div {
                id = "sidebar-overlay"
                classes = setOf("fixed", "inset-0", "bg-black", "bg-opacity-50", "z-30", "lg:hidden", "hidden")
            }

            // Main Content
            main(classes = "flex-1 w-full lg:w-auto p-4 lg:p-8") {
                // Page Title
                div(classes = "mb-6") {
                    h2(classes = "text-3xl font-bold text-gray-800 dark:text-white") {
                        +pageTitle
                    }
                }

                // Flash Messages
                successMessage?.let { msg ->
                    div(
                        classes = "mb-6 bg-green-100 dark:bg-green-900 border border-green-400 dark:border-green-700 text-green-700 dark:text-green-200 px-4 py-3 rounded",
                    ) {
                        span { +msg }
                    }
                }

                errorMessage?.let { msg ->
                    div(
                        classes = "mb-6 bg-red-100 dark:bg-red-900 border border-red-400 dark:border-red-700 text-red-700 dark:text-red-200 px-4 py-3 rounded",
                    ) {
                        span { +msg }
                    }
                }

                // Page Content
                content()
            }
        }

        scripts()
    }
}

fun BODY.adminHeader(
    auth: AuthInfo?,
    csrf: CsrfInfo,
) {
    val username = auth?.email ?: "user@example.com"

    header(classes = "bg-gray-800 dark:bg-gray-950 text-white shadow-lg") {
        div(classes = "container mx-auto px-4 py-4 flex justify-between items-center") {
            div(classes = "flex items-center space-x-4") {
                // Mobile sidebar toggle button
                button {
                    id = "mobile-sidebar-toggle"
                    classes =
                        setOf(
                            "lg:hidden",
                            "p-2",
                            "rounded-lg",
                            "bg-gray-700",
                            "dark:bg-gray-800",
                            "hover:bg-gray-600",
                            "dark:hover:bg-gray-700",
                            "transition-colors",
                        )
                    attributes["aria-label"] = "Toggle sidebar"
                    unsafe { +hamburgerIcon() }
                }

                h1(classes = "text-xl lg:text-2xl font-bold") {
                    +"Family Archive Admin"
                }
            }

            div(classes = "flex items-center space-x-2 lg:space-x-4") {
                // Jobs link
                a(
                    href = "http://localhost:8000",
                    classes = "hidden md:inline-flex items-center text-sm hover:text-gray-300 dark:hover:text-gray-400 transition-colors",
                    target = "_blank",
                ) {
                    +"Jobs"
                }

                // View Site link
                a(
                    href = "/",
                    classes = "hidden md:inline-flex items-center text-sm hover:text-gray-300 dark:hover:text-gray-400 transition-colors",
                    target = "_blank",
                ) {
                    +"View Site"
                }

                // Dark Mode Toggle
                button {
                    attributes["onclick"] = "window.darkMode.toggle()"
                    classes =
                        setOf(
                            "p-2",
                            "rounded-lg",
                            "bg-gray-700",
                            "dark:bg-gray-800",
                            "hover:bg-gray-600",
                            "dark:hover:bg-gray-700",
                            "transition-colors",
                        )
                    attributes["aria-label"] = "Toggle dark mode"
                    unsafe { +sunIcon("w-5 h-5 text-white hidden dark:block") }
                    unsafe { +moonIcon("w-5 h-5 text-white block dark:hidden") }
                }

                // User info
                span(classes = "text-sm text-gray-300 dark:text-gray-400 hidden md:inline") {
                    +username
                }

                // Logout
                form(action = "/logout", method = FormMethod.post, classes = "inline") {
                    hiddenInput(name = csrf.parameterName) {
                        value = csrf.token
                    }
                    button(
                        type = ButtonType.submit,
                        classes = "text-sm bg-gray-700 dark:bg-gray-800 hover:bg-gray-600 dark:hover:bg-gray-700 px-3 py-2 lg:px-4 rounded transition-colors",
                    ) {
                        +"Logout"
                    }
                }
            }
        }
    }
}

fun FlowContent.adminSidebar() {
    aside {
        id = "admin-sidebar"
        classes =
            setOf(
                "fixed",
                "overflow-auto",
                "lg:static",
                "inset-y-0",
                "left-0",
                "z-40",
                "w-64",
                "bg-white",
                "dark:bg-gray-800",
                "shadow-md",
                "min-h-screen",
                "transform",
                "-translate-x-full",
                "lg:translate-x-0",
                "transition-transform",
                "duration-300",
                "ease-in-out",
            )

        // Sidebar toggle button (mobile only)
        div(classes = "lg:hidden p-4 border-b border-gray-200 dark:border-gray-700") {
            button {
                id = "sidebar-toggle"
                classes =
                    setOf(
                        "p-2",
                        "rounded-lg",
                        "bg-gray-100",
                        "dark:bg-gray-700",
                        "hover:bg-gray-200",
                        "dark:hover:bg-gray-600",
                        "transition-colors",
                    )
                unsafe { +closeIcon("w-6 h-6 text-gray-700 dark:text-gray-300") }
            }
        }

        nav(classes = "p-4") {
            ul(classes = "space-y-2") {
                li {
                    a(
                        href = "/admin/dashboard",
                        classes = "block px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-700 dark:text-gray-300",
                    ) {
                        +"Dashboard"
                    }
                }
                li {
                    a(
                        href = "/admin/articles",
                        classes = "block px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-700 dark:text-gray-300",
                    ) {
                        +"Articles"
                    }
                }
                li {
                    div(classes = "px-4 py-2 text-gray-700 dark:text-gray-300 font-semibold") {
                        +"Artifacts"
                    }
                    ul(classes = "ml-4 mt-1 space-y-1") {
                        li {
                            a(
                                href = "/admin/artifacts",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"All Artifacts"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/photos",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Photos"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/letters",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Letters"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/documents",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Documents"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/ledgers",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Ledgers"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/audio",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Audio"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/videos",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Videos"
                            }
                        }
                        li {
                            a(
                                href = "/admin/artifacts/other",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Other"
                            }
                        }
                    }
                }
                li {
                    div(classes = "px-4 py-2 text-gray-700 dark:text-gray-300 font-semibold") {
                        +"Genealogy"
                    }
                    ul(classes = "ml-4 mt-1 space-y-1") {
                        li {
                            a(
                                href = "/admin/gedcom/import",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Import GEDCOM"
                            }
                        }
                        li {
                            a(
                                href = "/admin/individuals",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Individuals"
                            }
                        }
                        li {
                            a(
                                href = "/admin/families",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Families"
                            }
                        }
                        li {
                            a(
                                href = "/admin/places",
                                classes = "block px-4 py-1.5 rounded text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-600 dark:text-gray-400",
                            ) {
                                +"Places"
                            }
                        }
                    }
                }
                li {
                    a(
                        href = "/admin/users",
                        classes = "block px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-700 dark:text-gray-300",
                    ) {
                        +"Users"
                    }
                }
                li {
                    a(
                        href = "/admin/system/utilities",
                        classes = "block px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-700 dark:text-gray-300 flex items-center gap-2",
                    ) {
                        unsafe { +settingsIcon() }
                        +"System Utilities"
                    }
                }
            }
        }
    }
}
