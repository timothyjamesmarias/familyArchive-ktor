package com.timothymarias.familyarchive.views.public

import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.views.renderHtml
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.script

object FamilyTreeViews {
    fun familyTree(
        ctx: ViewContext,
        isAuthenticated: Boolean,
    ): String =
        renderHtml {
            pageLayout(
                title = "Family Tree - Marias Family Archive",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                head = {
                    if (ctx.isDevMode) {
                        // Development mode: Vite dev server
                        script(src = "http://localhost:5173/js/apps/family-tree.ts") {
                            attributes["type"] = "module"
                        }
                        link(rel = "stylesheet", href = "http://localhost:5173/css/family-tree/base.css")
                    } else {
                        // Production mode: Built assets
                        link(rel = "stylesheet", href = "/dist/assets/family-tree-css.css")
                        script(src = "/dist/assets/family-tree.js") {
                            attributes["type"] = "module"
                        }
                    }
                },
            ) {
                div {
                    id = "family-tree-root"
                    attributes["data-auth"] = isAuthenticated.toString()
                    // Family tree app will mount here
                }
            }
        }
}
