package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.Article
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.hiddenInput
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.textArea
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.repository.Page

/**
 * View functions for admin articles pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminArticlesViews {
    /**
     * Renders the articles index page with pagination.
     */
    fun index(
        ctx: AdminViewContext,
        articles: Page<Article>,
    ): String =
        renderHtml {
            adminLayout(
                title = "Articles - Admin",
                pageTitle = "Articles",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Articles" }
                    div {
                        a(href = "/admin/articles/new", classes = "btn-primary") {
                            +"New Article"
                        }
                    }
                }

                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden") {
                    div(classes = "table-responsive") {
                        table(classes = "min-w-full divide-y divide-gray-200 dark:divide-gray-700") {
                            thead(classes = "bg-gray-50 dark:bg-gray-700") {
                                tr {
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Title"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Slug"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Status"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Updated"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Actions"
                                    }
                                }
                            }
                            tbody(classes = "bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700") {
                                articles.content.forEach { article ->
                                    tr {
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100",
                                        ) {
                                            span { +article.title }
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            code { +article.slug }
                                        }
                                        td(classes = "px-6 py-4 whitespace-nowrap") {
                                            if (article.isPublished) {
                                                span(
                                                    classes = "px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200",
                                                ) {
                                                    +"Published"
                                                }
                                            } else {
                                                span(
                                                    classes = "px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-gray-200",
                                                ) {
                                                    +"Draft"
                                                }
                                            }
                                        }
                                        td(
                                            classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400",
                                        ) {
                                            span {
                                                +article.updatedAt.format(
                                                    java.time.format.DateTimeFormatter
                                                        .ofPattern("MMM dd, yyyy"),
                                                )
                                            }
                                        }
                                        td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                                            a(
                                                href = "/admin/articles/${article.id}/edit",
                                                classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
                                            ) {
                                                +"Edit"
                                            }
                                            form(
                                                action = "/admin/articles/${article.id}/delete",
                                                method = FormMethod.post,
                                                classes = "inline",
                                            ) {
                                                attributes["onsubmit"] =
                                                    "return confirm('Are you sure you want to delete this article?');"
                                                hiddenInput(name = ctx.csrf.parameterName) {
                                                    value =
                                                        ctx.csrf.token
                                                }
                                                button(
                                                    type = ButtonType.submit,
                                                    classes = "text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300",
                                                ) {
                                                    +"Delete"
                                                }
                                            }
                                        }
                                    }
                                }
                                if (articles.isEmpty) {
                                    tr {
                                        td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                                            attributes["colspan"] = "5"
                                            +"No articles found. "
                                            a(
                                                href = "/admin/articles/new",
                                                classes = "text-blue-600 dark:text-blue-400 hover:underline",
                                            ) {
                                                +"Create your first article"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pagination
                    if (articles.totalPages > 1) {
                        div(
                            classes = "bg-gray-50 dark:bg-gray-700 px-4 py-3 flex items-center justify-between border-t border-gray-200 dark:border-gray-600 sm:px-6",
                        ) {
                            div(classes = "flex-1 flex justify-between sm:hidden") {
                                if (!articles.isFirst) {
                                    a(
                                        href = "/admin/articles?page=${articles.number - 1}",
                                        classes = "relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700",
                                    ) {
                                        +"Previous"
                                    }
                                }
                                if (!articles.isLast) {
                                    a(
                                        href = "/admin/articles?page=${articles.number + 1}",
                                        classes = "ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700",
                                    ) {
                                        +"Next"
                                    }
                                }
                            }
                            div(classes = "hidden sm:flex-1 sm:flex sm:items-center sm:justify-between") {
                                div {
                                    p(classes = "text-sm text-gray-700 dark:text-gray-300") {
                                        +"Showing page "
                                        span { +"${articles.number + 1}" }
                                        +" of "
                                        span { +"${articles.totalPages}" }
                                    }
                                }
                                div {
                                    nav(classes = "relative z-0 inline-flex rounded-md shadow-sm -space-x-px") {
                                        if (!articles.isFirst) {
                                            a(
                                                href = "/admin/articles?page=${articles.number - 1}",
                                                classes = "relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                                            ) {
                                                +"Previous"
                                            }
                                        }
                                        if (!articles.isLast) {
                                            a(
                                                href = "/admin/articles?page=${articles.number + 1}",
                                                classes = "relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm font-medium text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                                            ) {
                                                +"Next"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the new article form.
     */
    fun newForm(ctx: AdminViewContext): String =
        renderHtml {
            adminLayout(
                title = "New Article - Admin",
                pageTitle = "New Article",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"New Article" }
                    p(classes = "mt-2 text-sm text-gray-600 dark:text-gray-400") {
                        +"Create a new article"
                    }
                }

                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/articles", method = FormMethod.post) {
                        hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }

                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "title"
                                +"Title"
                            }
                            input(
                                type = InputType.text,
                                name = "title",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "title"
                                required = true
                            }
                        }

                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "slug"
                                +"Slug"
                            }
                            input(
                                type = InputType.text,
                                name = "slug",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "slug"
                                required = true
                            }
                            p(classes = "mt-1 text-sm text-gray-500 dark:text-gray-400") {
                                +"URL-friendly version of the title (e.g., my-article-title)"
                            }
                        }

                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "excerpt"
                                +"Excerpt (Optional)"
                            }
                            textArea(
                                rows = "3",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "excerpt"
                                name = "excerpt"
                            }
                            p(classes = "mt-1 text-sm text-gray-500 dark:text-gray-400") {
                                +"Brief summary for article listings"
                            }
                        }

                        div(classes = "mb-6") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "content"
                                +"Content"
                            }
                            textArea(classes = "w-full") {
                                attributes["id"] = "content"
                                attributes["data-tinymce-editor"] = "true"
                                attributes["data-height"] = "600"
                                attributes["data-menubar"] = "true"
                                name = "content"
                            }
                        }

                        div(classes = "flex justify-between items-center") {
                            a(
                                href = "/admin/articles",
                                classes = "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200",
                            ) {
                                +"Cancel"
                            }
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Create Article"
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the edit article form.
     */
    fun edit(
        ctx: AdminViewContext,
        article: Article,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit Article - Admin",
                pageTitle = "Edit Article",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6 flex justify-between items-center") {
                    div {
                        h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +article.title }
                        p(classes = "mt-2 text-sm text-gray-600 dark:text-gray-400") {
                            if (article.isPublished) {
                                span(classes = "text-green-600 dark:text-green-400") { +"Published" }
                            } else {
                                span(classes = "text-gray-500 dark:text-gray-500") { +"Draft" }
                            }
                        }
                    }
                    div(classes = "flex gap-2") {
                        if (article.isDraft) {
                            form(
                                action = "/admin/articles/${article.id}/publish",
                                method = FormMethod.post,
                                classes = "inline",
                            ) {
                                hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }
                                button(type = ButtonType.submit, classes = "btn-primary") { +"Publish" }
                            }
                        } else {
                            form(
                                action = "/admin/articles/${article.id}/unpublish",
                                method = FormMethod.post,
                                classes = "inline",
                            ) {
                                hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }
                                button(
                                    type = ButtonType.submit,
                                    classes = "px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700",
                                ) {
                                    +"Unpublish"
                                }
                            }
                        }
                    }
                }
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6") {
                    form(action = "/admin/articles/${article.id}", method = FormMethod.post) {
                        hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "title"
                                +"Title"
                            }
                            input(
                                type = InputType.text,
                                name = "title",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "title"
                                value = article.title
                                required = true
                            }
                        }
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "slug"
                                +"Slug"
                            }
                            input(
                                type = InputType.text,
                                name = "slug",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "slug"
                                value = article.slug
                                required = true
                            }
                            p(classes = "mt-1 text-sm text-gray-500 dark:text-gray-400") {
                                +"URL-friendly version of the title (e.g., my-article-title)"
                            }
                        }
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "excerpt"
                                +"Excerpt (Optional)"
                            }
                            textArea(
                                rows = "3",
                                classes = "w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white",
                            ) {
                                attributes["id"] = "excerpt"
                                name = "excerpt"
                                +(article.excerpt ?: "")
                            }
                            p(classes = "mt-1 text-sm text-gray-500 dark:text-gray-400") {
                                +"Brief summary for article listings"
                            }
                        }
                        div(classes = "mb-6") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "content"
                                +"Content"
                            }
                            textArea(classes = "w-full") {
                                attributes["id"] = "content"
                                attributes["data-tinymce-editor"] = "true"
                                attributes["data-height"] = "600"
                                attributes["data-menubar"] = "true"
                                name = "content"
                                +article.content
                            }
                        }
                        div(classes = "flex justify-between items-center") {
                            a(
                                href = "/admin/articles",
                                classes = "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200",
                            ) {
                                +"Back to Articles"
                            }
                            button(type = ButtonType.submit, classes = "btn-primary") { +"Save Changes" }
                        }
                    }
                }
                div(
                    classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 border-2 border-red-200 dark:border-red-900",
                ) {
                    h3(classes = "text-lg font-semibold text-red-600 dark:text-red-400 mb-3") { +"Danger Zone" }
                    p(classes = "text-sm text-gray-600 dark:text-gray-400 mb-4") {
                        +"Deleting this article is permanent and cannot be undone."
                    }
                    form(action = "/admin/articles/${article.id}/delete", method = FormMethod.post) {
                        attributes["onsubmit"] =
                            "return confirm('Are you sure you want to delete this article? This action cannot be undone.');"
                        hiddenInput(name = ctx.csrf.parameterName) { value = ctx.csrf.token }
                        button(
                            type = ButtonType.submit,
                            classes = "px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700",
                        ) {
                            +"Delete Article"
                        }
                    }
                }
            }
        }
}
