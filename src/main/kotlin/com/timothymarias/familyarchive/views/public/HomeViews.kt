package com.timothymarias.familyarchive.views.public

import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.views.renderHtml
import kotlinx.html.div

object HomeViews {
    fun index(ctx: ViewContext): String =
        renderHtml {
            pageLayout(
                title = "Home - Marias Family Archive",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
            ) {
                div(classes = "max-w-6xl mx-auto") {
                    // Homepage content will go here
                }
            }
        }
}
