package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.dto.IndividualUpdateDto
import com.timothymarias.familyarchive.views.FamilyMember
import com.timothymarias.familyarchive.views.Individual
import com.timothymarias.familyarchive.views.IndividualEvent
import com.timothymarias.familyarchive.views.Place
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.TBODY
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hiddenInput
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.nav
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import com.timothymarias.familyarchive.repository.Page

/**
 * Enriched event with resolved place for display.
 */
data class EnrichedEvent(
    val event: IndividualEvent,
    val place: Place?,
)

/**
 * View functions for admin individuals pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminIndividualsViews {
    /**
     * Renders the individuals index page with search and pagination.
     */
    fun index(
        ctx: AdminViewContext,
        individuals: Page<Individual>,
        search: String?,
    ): String =
        renderHtml {
            adminLayout(
                title = "Individuals - Admin",
                pageTitle = "Individuals",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Header
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Individuals"
                    }
                }

                // Search form
                div(classes = "mb-6") {
                    form(action = "/admin/individuals", method = FormMethod.get, classes = "flex gap-2") {
                        input(
                            type = InputType.text,
                            name = "search",
                            classes = "flex-1 rounded-lg px-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                        ) {
                            value = search ?: ""
                            placeholder = "Search by name or GEDCOM ID..."
                        }
                        button(type = ButtonType.submit, classes = "btn-primary") { +"Search" }
                        if (!search.isNullOrEmpty()) {
                            a(href = "/admin/individuals", classes = "btn-secondary") { +"Clear" }
                        }
                    }
                }

                // Individuals table
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden") {
                    div(classes = "table-responsive") {
                        table(classes = "min-w-full divide-y divide-gray-200 dark:divide-gray-700") {
                            thead(classes = "bg-gray-50 dark:bg-gray-700") {
                                tr {
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"GEDCOM ID"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Name"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Sex"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Last Imported"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Actions"
                                    }
                                }
                            }
                            tbody(classes = "bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700") {
                                if (individuals.isEmpty) {
                                    renderEmptyTableRow(search)
                                } else {
                                    individuals.content.forEach { individual ->
                                        renderIndividualRow(individual, ctx)
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination
                renderPagination(individuals, search)
            }
        }

    /**
     * Renders the individual show/details page.
     */
    fun show(
        ctx: AdminViewContext,
        individual: Individual,
        events: List<EnrichedEvent> = emptyList(),
        familyMemberships: List<EnrichedFamilyMembership> = emptyList(),
    ): String =
        renderHtml {
            adminLayout(
                title = "Individual Details - Admin",
                pageTitle = "Individual Details",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(href = "/admin/individuals", classes = "text-blue-600 dark:text-blue-400 hover:underline") {
                        +"← Back to Individuals"
                    }
                }
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Individual Details" }
                    a(href = "/admin/individuals/${individual.id}/edit", classes = "btn-primary") {
                        +"Edit Individual"
                    }
                }
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6") {
                    h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") { +"Basic Information" }
                    div(classes = "grid grid-cols-1 md:grid-cols-2 gap-4") {
                        div {
                            p(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"GEDCOM ID" }
                            p(
                                classes = "mt-1 text-sm text-gray-900 dark:text-gray-100",
                            ) { +(individual.gedcomId ?: "-") }
                        }
                        div {
                            p(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Given Name" }
                            p(
                                classes = "mt-1 text-sm text-gray-900 dark:text-gray-100",
                            ) { +(individual.givenName ?: "-") }
                        }
                        div {
                            p(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Surname" }
                            p(
                                classes = "mt-1 text-sm text-gray-900 dark:text-gray-100",
                            ) { +(individual.surname ?: "-") }
                        }
                        div {
                            p(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Sex" }
                            p(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100") {
                                +(
                                    individual.sex?.toString()
                                        ?: "-"
                                )
                            }
                        }
                    }
                }
                if (events.isNotEmpty()) {
                    div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6") {
                        h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") { +"Events" }
                        div(classes = "space-y-3") {
                            events.forEach { enrichedEvent ->
                                div(classes = "border-l-4 border-blue-500 dark:border-blue-400 pl-4") {
                                    val eventType: String = enrichedEvent.event.eventType.name
                                    div(classes = "font-medium text-gray-900 dark:text-gray-100") { +eventType }
                                    div(classes = "text-sm text-gray-600 dark:text-gray-400") {
                                        val dateStr = enrichedEvent.event.dateParsed?.toString() ?: enrichedEvent.event.dateString
                                        val placeStr = enrichedEvent.place?.name
                                        if (dateStr != null) {
                                            +dateStr
                                        }
                                        if (placeStr != null) {
                                            +" - $placeStr"
                                        }
                                    }
                                    enrichedEvent.event.description?.let { desc ->
                                        div(classes = "text-sm text-gray-600 dark:text-gray-400 mt-1") {
                                            +desc
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                renderFamilyRelationships(familyMemberships)
            }
        }

    /**
     * Renders the new individual form page.
     */
    fun new(
        ctx: AdminViewContext,
        allIndividuals: List<Individual>,
    ): String =
        renderHtml {
            adminLayout(
                title = "New Individual - Admin",
                pageTitle = "New Individual",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(href = "/admin/individuals", classes = "text-blue-600 dark:text-blue-400 hover:underline") {
                        +"← Back to Individuals"
                    }
                }

                // Page title
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"New Individual"
                    }
                }

                // Create Form
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/individuals", method = FormMethod.post) {
                        hiddenInput(name = ctx.csrf.parameterName) {
                            value = ctx.csrf.token
                        }

                        // GEDCOM ID
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "gedcomId"
                                +"GEDCOM ID "
                                span(classes = "text-red-500") { +"*" }
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "gedcomId"
                                name = "gedcomId"
                                required = true
                            }
                        }

                        // Given Name
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "givenName"
                                +"Given Name"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "givenName"
                                name = "givenName"
                            }
                        }

                        // Surname
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "surname"
                                +"Surname"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "surname"
                                name = "surname"
                            }
                        }

                        // Sex
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "sex"
                                +"Sex"
                            }
                            select(
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "sex"
                                name = "sex"
                                option {
                                    value = ""
                                    +"Unknown"
                                }
                                option {
                                    value = "M"
                                    +"Male"
                                }
                                option {
                                    value = "F"
                                    +"Female"
                                }
                            }
                        }

                        // Is Tree Root
                        div(classes = "mb-4") {
                            div(classes = "flex items-center") {
                                checkBoxInput(
                                    classes = "rounded border-gray-300 dark:border-gray-600 dark:bg-gray-700 text-blue-600 shadow-sm focus:border-blue-500 focus:ring-blue-500",
                                ) {
                                    attributes["id"] = "isTreeRoot"
                                    name = "isTreeRoot"
                                    value = "true"
                                }
                                label(classes = "ml-2 block text-sm text-gray-700 dark:text-gray-300") {
                                    htmlFor = "isTreeRoot"
                                    +"Set as tree root"
                                }
                            }
                        }

                        // Buttons
                        div(classes = "flex gap-2 mt-6") {
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Create Individual"
                            }
                            a(href = "/admin/individuals", classes = "btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }
            }
        }

    /**
     * Renders the individual edit page.
     */
    fun edit(
        ctx: AdminViewContext,
        individual: Individual,
        allIndividuals: List<Individual>,
        individualUpdateDto: IndividualUpdateDto,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit Individual - Admin",
                pageTitle = "Edit Individual",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(
                        href = "/admin/individuals/${individual.id}",
                        classes = "text-blue-600 dark:text-blue-400 hover:underline",
                    ) {
                        +"← Back to Individual"
                    }
                }

                // Page title
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Edit Individual"
                    }
                }

                // Edit Form
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/individuals/${individual.id}/update", method = FormMethod.post) {
                        attributes["id"] = "individualEditForm"

                        hiddenInput(name = ctx.csrf.parameterName) {
                            value = ctx.csrf.token
                        }

                        // GEDCOM ID
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "gedcomId"
                                +"GEDCOM ID "
                                span(classes = "text-red-500") { +"*" }
                            }
                            select(
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "gedcomId"
                                name = "gedcomId"
                                allIndividuals.forEach { ind ->
                                    option {
                                        value = ind.gedcomId ?: ""
                                        selected = (ind.gedcomId == individualUpdateDto.gedcomId)
                                        +(ind.gedcomId ?: "")
                                    }
                                }
                            }
                        }

                        // Given Name
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "givenName"
                                +"Given Name"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "givenName"
                                name = "givenName"
                                value = individualUpdateDto.givenName ?: ""
                            }
                        }

                        // Surname
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "surname"
                                +"Surname"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "surname"
                                name = "surname"
                                value = individualUpdateDto.surname ?: ""
                            }
                        }

                        // Sex
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "sex"
                                +"Sex"
                            }
                            select(
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "sex"
                                name = "sex"
                                option {
                                    value = ""
                                    selected = (individualUpdateDto.sex.isNullOrEmpty())
                                    +"Unknown"
                                }
                                option {
                                    value = "M"
                                    selected = ("M" == individualUpdateDto.sex)
                                    +"Male"
                                }
                                option {
                                    value = "F"
                                    selected = ("F" == individualUpdateDto.sex)
                                    +"Female"
                                }
                                option {
                                    value = "U"
                                    selected = ("U" == individualUpdateDto.sex)
                                    +"Unknown"
                                }
                            }
                        }

                        // Tree Root
                        div(classes = "mb-4") {
                            div(classes = "flex items-start") {
                                div(classes = "flex items-center h-5") {
                                    input(
                                        type = InputType.checkBox,
                                        classes = "w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600",
                                    ) {
                                        attributes["id"] = "isTreeRoot"
                                        name = "isTreeRoot"
                                        checked = individualUpdateDto.isTreeRoot
                                    }
                                }
                                div(classes = "ml-3") {
                                    label(classes = "text-sm font-medium text-gray-700 dark:text-gray-300") {
                                        htmlFor = "isTreeRoot"
                                        +"Set as Tree Root"
                                    }
                                    p(classes = "text-xs text-gray-500 dark:text-gray-400 mt-1") {
                                        +"Mark this person as the root of the family tree. "
                                        strong { +"Only one person can be the tree root at a time." }
                                        +" If another person is already set as the tree root, you must unset them first."
                                    }
                                }
                            }
                        }

                        // Buttons
                        div(classes = "flex gap-2 mt-6") {
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Save Changes"
                            }
                            a(href = "/admin/individuals/${individual.id}", classes = "btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }

                // Warning Message
                div(
                    classes = "mt-4 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg",
                ) {
                    p(classes = "text-sm text-yellow-800 dark:text-yellow-200") {
                        strong { +"Note:" }
                        +" Changes to this individual will affect their appearance in the family tree and all associated family relationships."
                    }
                }
            }
        }

    // Helper Functions

    private fun FlowContent.renderPagination(
        individuals: Page<Individual>,
        search: String?,
    ) {
        if (individuals.totalPages > 1) {
            div(classes = "mt-6 flex items-center justify-between") {
                renderPaginationStats(individuals.numberOfElements, individuals.totalElements, "individuals")
                renderPaginationNav(individuals, search)
            }
        } else if (individuals.totalElements > 0) {
            div(classes = "mt-4 text-sm text-gray-600 dark:text-gray-400") {
                renderPaginationStats(individuals.numberOfElements, individuals.totalElements, "individuals")
            }
        }
    }

    private fun FlowContent.renderPaginationStats(
        showing: Int,
        total: Long,
        entityName: String,
    ) {
        div(classes = "text-sm text-gray-600 dark:text-gray-400") {
            +"Showing "
            span { +"$showing" }
            +" of "
            span { +"$total" }
            +" total $entityName"
        }
    }

    private fun FlowContent.renderPaginationNav(
        page: Page<*>,
        search: String?,
    ) {
        val baseUrl = "/admin/individuals"
        nav(classes = "flex items-center gap-2") {
            renderPreviousButton(page, baseUrl, search)
            renderPageNumbers(page, baseUrl, search)
            renderNextButton(page, baseUrl, search)
        }
    }

    private fun FlowContent.renderPreviousButton(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        val isEnabled = page.number > 0
        val url = "$baseUrl?page=${page.number - 1}&size=20&search=${search ?: ""}"

        if (isEnabled) {
            a(
                href = url,
                classes = "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700",
            ) {
                +"Previous"
            }
        } else {
            span(
                classes = "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed",
            ) {
                +"Previous"
            }
        }
    }

    private fun FlowContent.renderPageNumbers(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        div(classes = "flex gap-1") {
            for (i in 0 until page.totalPages) {
                if (i in (page.number - 2)..(page.number + 2)) {
                    renderPageNumber(i, page.number, baseUrl, search)
                }
            }
        }
    }

    private fun FlowContent.renderPageNumber(
        pageNum: Int,
        currentPage: Int,
        baseUrl: String,
        search: String?,
    ) {
        val isCurrent = pageNum == currentPage
        val url = "$baseUrl?page=$pageNum&size=20&search=${search ?: ""}"

        if (isCurrent) {
            span(classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-blue-600 text-white border-blue-600") {
                +"${pageNum + 1}"
            }
        } else {
            a(
                href = url,
                classes = "px-3 py-2 text-sm font-medium border rounded-lg bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700",
            ) {
                +"${pageNum + 1}"
            }
        }
    }

    private fun FlowContent.renderNextButton(
        page: Page<*>,
        baseUrl: String,
        search: String?,
    ) {
        val isEnabled = page.number < page.totalPages - 1
        val url = "$baseUrl?page=${page.number + 1}&size=20&search=${search ?: ""}"

        if (isEnabled) {
            a(
                href = url,
                classes = "px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700",
            ) {
                +"Next"
            }
        } else {
            span(
                classes = "px-3 py-2 text-sm font-medium text-gray-400 dark:text-gray-600 bg-gray-100 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg cursor-not-allowed",
            ) {
                +"Next"
            }
        }
    }

    private fun TBODY.renderEmptyTableRow(search: String?) {
        tr {
            td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                attributes["colspan"] = "5"
                val message =
                    if (!search.isNullOrEmpty()) {
                        "No individuals found matching \"$search\""
                    } else {
                        "No individuals found. Import a GEDCOM file to get started."
                    }
                +message
            }
        }
    }

    private fun TBODY.renderIndividualRow(
        individual: Individual,
        ctx: AdminViewContext,
    ) {
        tr {
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +(individual.gedcomId ?: "-") }
            }
            td(classes = "px-6 py-4 text-sm text-gray-900 dark:text-gray-100") {
                span { +formatIndividualName(individual) }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +(individual.sex?.toString() ?: "-") }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +formatLastImportedDate(individual) }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                renderIndividualActions(individual, ctx)
            }
        }
    }

    private fun formatIndividualName(individual: Individual): String =
        buildString {
            individual.givenName?.let { append(it) }
            individual.surname?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }.ifEmpty { "-" }

    private fun formatLastImportedDate(individual: Individual): String =
        individual.lastImportedAt?.format(
            java.time.format.DateTimeFormatter
                .ofPattern("MMM dd, yyyy"),
        ) ?: "-"

    private fun FlowContent.renderFamilyRelationships(familyMemberships: List<EnrichedFamilyMembership>) {
        if (familyMemberships.isEmpty()) return

        div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
            h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                +"Family Relationships"
            }
            div(classes = "space-y-2") {
                familyMemberships.forEach { membership ->
                    renderFamilyMembership(membership)
                }
            }
        }
    }

    private fun FlowContent.renderFamilyMembership(membership: EnrichedFamilyMembership) {
        div(classes = "text-sm") {
            val role = membership.member.role.name
            span(classes = "font-medium text-gray-900 dark:text-gray-100") { +role }
            span(classes = "text-gray-600 dark:text-gray-400") { +" in family " }
            a(
                href = "/admin/families/${membership.family.id}/edit",
                classes = "text-blue-600 dark:text-blue-400 hover:underline",
            ) {
                +(membership.family.gedcomId ?: "")
            }
        }
    }

    private fun TD.renderIndividualActions(
        individual: Individual,
        ctx: AdminViewContext,
    ) {
        a(
            href = "/admin/individuals/${individual.id}",
            classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
        ) {
            +"View"
        }
        form(
            action = "/admin/individuals/${individual.id}/delete",
            method = FormMethod.post,
            classes = "inline",
        ) {
            attributes["onsubmit"] =
                "return confirm('Are you sure you want to delete this individual? This action cannot be undone.');"
            hiddenInput(name = ctx.csrf.parameterName) {
                value = ctx.csrf.token
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
