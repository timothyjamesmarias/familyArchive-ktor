package com.timothymarias.familyarchive.components

import com.timothymarias.familyarchive.components.IconHelpers.closeIcon
import com.timothymarias.familyarchive.components.IconHelpers.hamburgerIcon
import com.timothymarias.familyarchive.components.IconHelpers.moonIcon
import com.timothymarias.familyarchive.components.IconHelpers.sunIcon
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.MAIN
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.head
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.unsafe
import com.timothymarias.familyarchive.views.AuthInfo

/**
 * Main page layout component with navigation, header, footer, and content area.
 * Includes dark mode support and authentication-aware navigation.
 */
fun HTML.pageLayout(
    title: String = "Family Archive",
    isDevMode: Boolean,
    auth: AuthInfo? = null,
    head: HEAD.() -> Unit = {},
    scripts: BODY.() -> Unit = {},
    content: MAIN.() -> Unit,
) {
    lang = "en"
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title(content = title)

        // Prevent dark mode flicker by applying theme before page renders
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
            // Development mode: Vite dev server
            script(src = "http://localhost:5173/@vite/client") {
                attributes["type"] = "module"
            }
            script(src = "http://localhost:5173/js/main.ts") {
                attributes["type"] = "module"
            }
            link(rel = "stylesheet", href = "http://localhost:5173/css/front.css")
        } else {
            // Production mode: Built assets
            link(rel = "stylesheet", href = "/dist/assets/main.css")
            script(src = "/dist/assets/app.js") {
                attributes["type"] = "module"
            }
        }

        head()
    }

    body(classes = "bg-stone-100 dark:bg-stone-950 min-h-screen transition-colors flex flex-col font-sans") {
        // Navigation
        nav(classes = "bg-stone-50 dark:bg-stone-900 border-b border-stone-200 dark:border-stone-800") {
            div(classes = "container mx-auto px-4 py-5") {
                div(classes = "flex justify-between items-center") {
                    // Logo
                    a(
                        href = "/",
                        classes = "text-2xl font-serif font-semibold text-stone-900 dark:text-stone-50 hover:text-stone-700 dark:hover:text-stone-300 transition-colors",
                    ) {
                        +"Family Archive"
                    }

                    // Desktop Navigation
                    div(classes = "hidden md:flex items-center space-x-6") {
                        navigationLink(href = "/family-tree", text = "Family Tree")
                        navigationLink(href = "/artifacts", text = "Artifacts")
                        navigationLink(href = "/articles", text = "Articles")

                        if (auth != null && auth.isAuthenticated) {
                            navigationLink(href = "/admin/dashboard", text = "Dashboard")
                        } else {
                            navigationLink(href = "/login", text = "Admin Login")
                        }

                        darkModeToggle()
                    }

                    // Mobile Menu Button
                    div(classes = "md:hidden flex items-center space-x-2") {
                        darkModeToggle()
                        mobileMenuButton()
                    }
                }

                // Mobile Navigation Menu
                mobileMenu(auth)
            }
        }

        // Main content
        main(classes = "container mx-auto px-4 py-12 flex-grow") {
            content()
        }

        // Footer
        footer(classes = "bg-stone-50 dark:bg-stone-900 border-t border-stone-200 dark:border-stone-800") {
            div(classes = "container mx-auto px-4 py-8 text-center text-stone-600 dark:text-stone-400") {
                p(classes = "font-serif") {
                    +"© Marias Family Archive"
                }
            }
        }

        scripts()
    }
}

/**
 * Navigation link component with consistent styling
 */
private fun FlowContent.navigationLink(
    href: String,
    text: String,
) {
    a(
        href = href,
        classes = "text-stone-700 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-50 font-medium transition-colors",
    ) {
        +text
    }
}

/**
 * Dark mode toggle button with sun/moon icons
 */
private fun FlowContent.darkModeToggle() {
    button(
        classes = "p-2 rounded-md bg-stone-100 dark:bg-stone-800 hover:bg-stone-200 dark:hover:bg-stone-700 transition-colors border border-stone-200 dark:border-stone-700",
    ) {
        attributes["onclick"] = "window.darkMode.toggle()"
        attributes["aria-label"] = "Toggle dark mode"

        // Sun icon (shows in dark mode)
        unsafe { +sunIcon() }

        // Moon icon (shows in light mode)
        unsafe { +moonIcon() }
    }
}

/**
 * Mobile menu hamburger button
 */
private fun FlowContent.mobileMenuButton() {
    button(
        classes = "p-2 rounded-md text-stone-700 dark:text-stone-300 hover:bg-stone-200 dark:hover:bg-stone-800 transition-colors",
    ) {
        attributes["onclick"] = "window.mobileMenu.toggle()"
        attributes["aria-label"] = "Toggle menu"
        attributes["id"] = "mobile-menu-button"

        // Hamburger icon (default)
        span(classes = "block") {
            attributes["id"] = "hamburger-icon"
            unsafe { +hamburgerIcon() }
        }

        // Close icon (hidden by default)
        span(classes = "hidden") {
            attributes["id"] = "close-icon"
            unsafe { +closeIcon() }
        }
    }
}

/**
 * Mobile navigation menu
 */
private fun FlowContent.mobileMenu(auth: AuthInfo?) {
    div(classes = "hidden md:hidden") {
        attributes["id"] = "mobile-menu"
        div(classes = "px-2 pt-2 pb-3 space-y-1") {
            mobileNavigationLink(href = "/family-tree", text = "Family Tree")
            mobileNavigationLink(href = "/artifacts", text = "Artifacts")
            mobileNavigationLink(href = "/articles", text = "Articles")

            if (auth != null && auth.isAuthenticated) {
                mobileNavigationLink(href = "/admin/dashboard", text = "Dashboard")
            } else {
                mobileNavigationLink(href = "/login", text = "Admin Login")
            }
        }
    }
}

/**
 * Mobile navigation link component
 */
private fun FlowContent.mobileNavigationLink(
    href: String,
    text: String,
) {
    a(
        href = href,
        classes = "block px-3 py-2 rounded-md text-base font-medium text-stone-700 dark:text-stone-300 hover:bg-stone-200 dark:hover:bg-stone-800 hover:text-stone-900 dark:hover:text-stone-50 transition-colors",
    ) {
        +text
    }
}
