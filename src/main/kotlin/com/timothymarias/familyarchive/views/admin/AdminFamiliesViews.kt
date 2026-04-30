package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.dto.FamilyUpdateDto
import com.timothymarias.familyarchive.views.Family
import com.timothymarias.familyarchive.views.FamilyMember
import com.timothymarias.familyarchive.views.Individual
import com.timothymarias.familyarchive.views.Place
import com.timothymarias.familyarchive.model.FamilyRole
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.TBODY
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.dd
import kotlinx.html.div
import kotlinx.html.dl
import kotlinx.html.dt
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
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
 * Enriched family member with the resolved individual for display.
 */
data class EnrichedFamilyMember(
    val member: FamilyMember,
    val individual: Individual,
)

/**
 * Enriched family membership for displaying an individual's family relationships.
 */
data class EnrichedFamilyMembership(
    val member: FamilyMember,
    val family: Family,
)

/**
 * View-level summary for a family row in the index table.
 */
data class FamilyRowData(
    val family: Family,
    val memberCount: Int,
    val marriagePlaceName: String?,
)

/**
 * View functions for admin families pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminFamiliesViews {
    /**
     * Renders the families index page with search and pagination.
     */
    fun index(
        ctx: AdminViewContext,
        families: Page<FamilyRowData>,
        search: String?,
    ): String =
        renderHtml {
            adminLayout(
                title = "Families - Admin",
                pageTitle = "Families",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Header
                div(classes = "mb-6 flex justify-between items-center") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Families" }
                }

                // Search form
                div(classes = "mb-6") {
                    form(action = "/admin/families", method = FormMethod.get, classes = "flex gap-2") {
                        input(
                            type = InputType.text,
                            name = "search",
                            classes = "flex-1 rounded-lg px-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                        ) {
                            value = search ?: ""
                            placeholder = "Search by GEDCOM ID, member name, or place..."
                        }
                        button(type = ButtonType.submit, classes = "btn-primary") { +"Search" }
                        if (!search.isNullOrEmpty()) {
                            a(href = "/admin/families", classes = "btn-secondary") { +"Clear" }
                        }
                    }
                }

                // Families table
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
                                        +"Members"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Marriage Date"
                                    }
                                    th(
                                        classes = "px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider",
                                    ) {
                                        +"Marriage Place"
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
                                if (families.isEmpty) {
                                    renderEmptyTableRow()
                                } else {
                                    families.content.forEach { rowData ->
                                        renderFamilyRow(rowData, ctx)
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination
                renderPagination(families, search)
            }
        }

    /**
     * Renders the new family form page.
     */
    fun newFamily(ctx: AdminViewContext): String =
        renderHtml {
            adminLayout(
                title = "New Family - Admin",
                pageTitle = "New Family",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    p(classes = "text-gray-600 dark:text-gray-400") {
                        +"Create new family form"
                    }
                }
            }
        }

    // Helper Functions

    private fun TBODY.renderEmptyTableRow() {
        tr {
            td(classes = "px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400") {
                attributes["colspan"] = "6"
                +"No families found. Import a GEDCOM file to get started."
            }
        }
    }

    private fun TBODY.renderFamilyRow(
        rowData: FamilyRowData,
        ctx: AdminViewContext,
    ) {
        val family = rowData.family
        tr {
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +(family.gedcomId ?: "") }
            }
            td(classes = "px-6 py-4 text-sm text-gray-900 dark:text-gray-100") {
                span { +"${rowData.memberCount} member(s)" }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +formatDate(family.marriageDateParsed, family.marriageDateString) }
            }
            td(classes = "px-6 py-4 text-sm text-gray-500 dark:text-gray-400") {
                span { +(rowData.marriagePlaceName ?: "-") }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400") {
                span { +formatLastImportedDate(family) }
            }
            td(classes = "px-6 py-4 whitespace-nowrap text-right text-sm font-medium") {
                renderFamilyActions(family, ctx)
            }
        }
    }

    private fun formatDate(
        parsedDate: java.time.LocalDateTime?,
        stringDate: String?,
    ): String =
        parsedDate?.toLocalDate()?.format(
            java.time.format.DateTimeFormatter
                .ofPattern("MMM dd, yyyy"),
        )
            ?: stringDate
            ?: "-"

    private fun formatLastImportedDate(family: Family): String =
        family.lastImportedAt?.toLocalDate()?.format(
            java.time.format.DateTimeFormatter
                .ofPattern("MMM dd, yyyy"),
        ) ?: "-"

    private fun TD.renderFamilyActions(
        family: Family,
        ctx: AdminViewContext,
    ) {
        a(
            href = "/admin/families/${family.id}",
            classes = "text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3",
        ) {
            +"View"
        }
        form(
            action = "/admin/families/${family.id}/delete",
            method = FormMethod.post,
            classes = "inline",
        ) {
            attributes["onsubmit"] =
                "return confirm('Are you sure you want to delete this family? This action cannot be undone.');"
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

    private fun FlowContent.renderPagination(
        families: Page<FamilyRowData>,
        search: String?,
    ) {
        if (families.totalPages > 1) {
            div(classes = "mt-6 flex items-center justify-between") {
                renderPaginationStats(families.numberOfElements, families.totalElements)
                renderPaginationNav(families, search)
            }
        } else if (families.totalElements > 0) {
            div(classes = "mt-4 text-sm text-gray-600 dark:text-gray-400") {
                renderPaginationStats(families.numberOfElements, families.totalElements)
            }
        }
    }

    private fun FlowContent.renderPaginationStats(
        showing: Int,
        total: Long,
    ) {
        div(classes = "text-sm text-gray-600 dark:text-gray-400") {
            +"Showing "
            span { +"$showing" }
            +" of "
            span { +"$total" }
            +" total families"
        }
    }

    private fun FlowContent.renderPaginationNav(
        page: Page<*>,
        search: String?,
    ) {
        val baseUrl = "/admin/families"
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

    private fun FlowContent.renderFamilyMembers(
        family: Family,
        members: List<EnrichedFamilyMember>,
    ) {
        div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
            h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") {
                +"Family Members"
            }

            if (members.isEmpty()) {
                div(classes = "text-sm text-gray-500 dark:text-gray-400") {
                    +"No family members recorded."
                }
                return@div
            }

            div(classes = "space-y-4") {
                renderParents(family, members)
                renderChildren(family, members)
            }
        }
    }

    private fun FlowContent.renderParents(
        family: Family,
        members: List<EnrichedFamilyMember>,
    ) {
        val parents = members.filter {
            it.member.role == FamilyRole.FATHER || it.member.role == FamilyRole.MOTHER
        }

        if (parents.isEmpty()) return

        div {
            h3(classes = "text-sm font-medium text-gray-500 dark:text-gray-400 mb-2") {
                +"Parents"
            }
            div(classes = "space-y-2") {
                parents.forEach { enriched ->
                    renderFamilyMember(enriched, "blue")
                }
            }
        }
    }

    private fun FlowContent.renderChildren(
        family: Family,
        members: List<EnrichedFamilyMember>,
    ) {
        val children = members.filter {
            it.member.role == FamilyRole.CHILD
        }

        if (children.isEmpty()) return

        div {
            h3(classes = "text-sm font-medium text-gray-500 dark:text-gray-400 mb-2") {
                +"Children"
            }
            div(classes = "space-y-2") {
                children.forEach { enriched ->
                    renderFamilyMember(enriched, "green")
                }
            }
        }
    }

    private fun FlowContent.renderFamilyMember(
        enriched: EnrichedFamilyMember,
        badgeColor: String,
    ) {
        val individual = enriched.individual
        div(classes = "flex items-center") {
            span(
                classes = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-$badgeColor-100 text-$badgeColor-800 dark:bg-$badgeColor-900 dark:text-$badgeColor-300 mr-3",
            ) {
                +enriched.member.role.toString()
            }
            a(
                href = "/admin/individuals/${individual.id}/edit",
                classes = "text-blue-600 dark:text-blue-400 hover:underline",
            ) {
                +getIndividualDisplayName(individual)
            }
        }
    }

    private fun getIndividualDisplayName(individual: Individual): String =
        when {
            individual.givenName != null && individual.surname != null ->
                "${individual.givenName} ${individual.surname}"
            individual.givenName != null ->
                individual.givenName!!
            individual.surname != null ->
                individual.surname!!
            else ->
                individual.gedcomId ?: ""
        }

    /**
     * Renders the family show/details page.
     */
    fun show(
        ctx: AdminViewContext,
        family: Family,
        members: List<EnrichedFamilyMember>,
        marriagePlace: Place?,
    ): String =
        renderHtml {
            adminLayout(
                title = "Family Details - Admin",
                pageTitle = "Family Details",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(href = "/admin/families", classes = "text-blue-600 dark:text-blue-400 hover:underline") {
                        +"← Back to Families"
                    }
                }

                // Header
                div(classes = "mb-6") {
                    div(classes = "flex justify-between items-center") {
                        h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") { +"Family Details" }
                        a(href = "/admin/families/${family.id}/edit", classes = "btn-primary") {
                            +"Edit Family"
                        }
                    }
                }

                // Family Details
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6") {
                    h2(classes = "text-xl font-semibold text-gray-900 dark:text-white mb-4") { +"Basic Information" }

                    dl(classes = "grid grid-cols-1 md:grid-cols-2 gap-4") {
                        // GEDCOM ID
                        dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"GEDCOM ID" }
                        dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-4") { +(family.gedcomId ?: "") }

                        // Marriage Date
                        dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Marriage Date" }
                        dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-4") {
                            family.marriageDateParsed?.let {
                                span {
                                    +it.format(
                                        java.time.format.DateTimeFormatter
                                            .ofPattern("MMM dd, yyyy"),
                                    )
                                }
                            } ?: family.marriageDateString?.let {
                                span { +it }
                            } ?: span { +"-" }
                        }

                        // Marriage Place
                        dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Marriage Place" }
                        dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-4") {
                            marriagePlace?.let {
                                a(
                                    href = "/admin/places/${it.id}/edit",
                                    classes = "text-blue-600 dark:text-blue-400 hover:underline",
                                ) {
                                    +it.name
                                }
                            } ?: span { +"-" }
                        }

                        // Divorce Date
                        dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Divorce Date" }
                        dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-4") {
                            family.divorceDateParsed?.let {
                                span {
                                    +it.format(
                                        java.time.format.DateTimeFormatter
                                            .ofPattern("MMM dd, yyyy"),
                                    )
                                }
                            } ?: family.divorceDateString?.let {
                                span { +it }
                            } ?: span { +"-" }
                        }

                        // Last Imported
                        dt(classes = "text-sm font-medium text-gray-500 dark:text-gray-400") { +"Last Imported" }
                        dd(classes = "mt-1 text-sm text-gray-900 dark:text-gray-100 mb-4") {
                            family.lastImportedAt?.let {
                                span {
                                    +it.format(
                                        java.time.format.DateTimeFormatter
                                            .ofPattern("MMM dd, yyyy HH:mm"),
                                    )
                                }
                            } ?: span { +"-" }
                        }
                    }
                }

                // Family Members
                renderFamilyMembers(family, members)
            }
        }

    /**
     * Renders the family edit page.
     */
    fun edit(
        ctx: AdminViewContext,
        family: Family,
        places: List<Place>,
        familyUpdateDto: FamilyUpdateDto,
    ): String =
        renderHtml {
            adminLayout(
                title = "Edit Family - Admin",
                pageTitle = "Edit Family",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                // Back link
                div(classes = "mb-6") {
                    a(
                        href = "/admin/families/${family.id}",
                        classes = "text-blue-600 dark:text-blue-400 hover:underline",
                    ) {
                        +"← Back to Family"
                    }
                }

                // Page title
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Edit Family"
                    }
                }

                // Edit Form
                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/families/${family.id}/update", method = FormMethod.post) {
                        attributes["id"] = "familyEditForm"

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
                                value = familyUpdateDto.gedcomId ?: ""
                            }
                        }

                        // Marriage Date String
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "marriageDateString"
                                +"Marriage Date"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "marriageDateString"
                                name = "marriageDateString"
                                value = familyUpdateDto.marriageDateString ?: ""
                                placeholder = "e.g., 15 JUL 1990 or July 15, 1990"
                            }
                            p(classes = "mt-1 text-xs text-gray-500 dark:text-gray-400") {
                                +"Enter date as text (GEDCOM format or natural language)"
                            }
                        }

                        // Marriage Place
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "marriagePlaceId"
                                +"Marriage Place"
                            }
                            select(
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "marriagePlaceId"
                                name = "marriagePlaceId"
                                option {
                                    value = ""
                                    selected = (familyUpdateDto.marriagePlaceId == null)
                                    +"No place"
                                }
                                places.forEach { place ->
                                    option {
                                        value = place.id.toString()
                                        selected = (familyUpdateDto.marriagePlaceId == place.id)
                                        +place.name
                                    }
                                }
                            }
                        }

                        // Divorce Date String
                        div(classes = "mb-4") {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                htmlFor = "divorceDateString"
                                +"Divorce Date"
                            }
                            input(
                                type = InputType.text,
                                classes = "w-full rounded-lg px-3 py-2 border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm focus:border-blue-500 focus:ring-blue-500",
                            ) {
                                attributes["id"] = "divorceDateString"
                                name = "divorceDateString"
                                value = familyUpdateDto.divorceDateString ?: ""
                                placeholder = "e.g., 20 DEC 2000 or December 20, 2000"
                            }
                            p(classes = "mt-1 text-xs text-gray-500 dark:text-gray-400") {
                                +"Enter date as text (GEDCOM format or natural language)"
                            }
                        }

                        // Buttons
                        div(classes = "flex gap-2 mt-6") {
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Save Changes"
                            }
                            a(href = "/admin/families/${family.id}", classes = "btn-secondary") {
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
                        +" Changes to this family will affect all associated individuals and their family tree relationships."
                    }
                }
            }
        }
}
