package com.timothymarias.familyarchive.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.header
import kotlinx.html.p
import kotlinx.html.svg
import kotlinx.html.unsafe

/**
 * Artifact type card component for the main artifacts index page.
 * Displays a clickable card with icon, title, and description.
 */
fun FlowContent.artifactTypeCard(
    href: String,
    title: String,
    description: String,
    icon: String,
) {
    a(
        href = href,
        classes =
            "block bg-stone-50 dark:bg-stone-900 border border-stone-200 dark:border-stone-800 rounded-md p-6 " +
                "hover:border-stone-300 dark:hover:border-stone-700 hover:bg-stone-100 dark:hover:bg-stone-850 " +
                "transition-all duration-150 group",
    ) {
        div(classes = "flex items-center justify-between") {
            div {
                h2(
                    classes =
                        "text-2xl font-serif font-semibold text-stone-900 dark:text-stone-50 mb-2 " +
                            "group-hover:text-sage-700 dark:group-hover:text-sage-400 transition-colors",
                ) {
                    +title
                }
                p(classes = "text-stone-600 dark:text-stone-400") {
                    +description
                }
            }
            svg(
                classes =
                    "w-8 h-8 text-stone-400 dark:text-stone-600 group-hover:text-stone-600 " +
                        "dark:group-hover:text-stone-400 transition-colors",
            ) {
                attributes["fill"] = "none"
                attributes["stroke"] = "currentColor"
                attributes["viewBox"] = "0 0 24 24"
                unsafe { +icon }
            }
        }
    }
}

/**
 * SVG icon components for different artifact types
 */
object ArtifactIcons {
    const val photo: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>"""

    const val video: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>"""

    const val audio: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path>"""

    const val letter: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>"""

    const val document: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>"""

    const val ledger: String =
        """<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>"""
}

/**
 * Page header component with title and description
 */
fun FlowContent.pageHeader(
    title: String,
    description: String,
) {
    header(classes = "mb-12 text-center") {
        h1(classes = "text-5xl font-serif font-semibold text-stone-900 dark:text-stone-50 mb-4 leading-tight") {
            +title
        }
        p(classes = "text-lg text-stone-600 dark:text-stone-400 leading-relaxed max-w-2xl mx-auto") {
            +description
        }
    }
}
