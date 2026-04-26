package com.timothymarias.familyarchive.views.admin

import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.components.IconHelpers.infoCircleFilledIcon
import com.timothymarias.familyarchive.components.adminLayout
import com.timothymarias.familyarchive.views.renderHtml
import kotlinx.html.ButtonType
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.hiddenInput
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul
import kotlinx.html.unsafe

/**
 * View functions for admin GEDCOM import pages.
 * Separates HTML rendering logic from controller routing/business logic.
 */
object AdminGedcomImportViews {
    /**
     * Renders the GEDCOM import form page.
     */
    fun importForm(ctx: AdminViewContext): String =
        renderHtml {
            adminLayout(
                title = "Import GEDCOM - Admin",
                pageTitle = "Import GEDCOM",
                isDevMode = ctx.isDevMode,
                auth = ctx.auth,
                csrf = ctx.csrf,
                successMessage = ctx.successMessage,
                errorMessage = ctx.errorMessage,
            ) {
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-gray-900 dark:text-white") {
                        +"Import GEDCOM File"
                    }
                    p(classes = "mt-2 text-sm text-gray-600 dark:text-gray-400") {
                        +"Upload a GEDCOM file to import family tree data into the archive. The import will run as a background job."
                    }
                }

                div(classes = "bg-white dark:bg-gray-800 rounded-lg shadow p-6") {
                    form(action = "/admin/gedcom/import", method = FormMethod.post, classes = "space-y-6") {
                        attributes["id"] = "uploadForm"
                        encType = FormEncType.multipartFormData

                        hiddenInput(name = ctx.csrf.parameterName) {
                            value = ctx.csrf.token
                        }

                        // Uppy File Upload
                        div {
                            label(classes = "block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2") {
                                +"GEDCOM File *"
                            }
                            div {
                                attributes["data-uppy-uploader"] = ""
                                attributes["data-form-id"] = "uploadForm"
                                attributes["data-field-name"] = "file"
                                attributes["data-allowed-types"] = ".ged"
                                attributes["data-max-file-size"] = "52428800"
                                attributes["data-max-number-of-files"] = "1"
                            }
                            p(classes = "mt-2 text-sm text-gray-500 dark:text-gray-400") {
                                +"Upload a GEDCOM (.ged) file. Maximum 50MB. The file will be processed in the background."
                            }
                        }

                        // Info Box
                        div(
                            classes = "bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4",
                        ) {
                            div(classes = "flex") {
                                div(classes = "flex-shrink-0") {
                                    unsafe { +infoCircleFilledIcon("h-5 w-5 text-blue-400") }
                                }
                                div(classes = "ml-3") {
                                    h3(classes = "text-sm font-medium text-blue-800 dark:text-blue-200") {
                                        +"About GEDCOM Imports"
                                    }
                                    div(classes = "mt-2 text-sm text-blue-700 dark:text-blue-300") {
                                        ul(classes = "list-disc list-inside space-y-1") {
                                            li { +"The import process runs in the background as a job" }
                                            li {
                                                +"You can monitor progress in the "
                                                a(
                                                    href = "http://localhost:8000",
                                                    target = "_blank",
                                                    classes = "underline",
                                                ) {
                                                    +"Jobs dashboard"
                                                }
                                            }
                                            li {
                                                +"All data is imported within a transaction - if anything fails, no partial data is saved"
                                            }
                                            li {
                                                +"Reimporting the same file will update existing records by GEDCOM ID"
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Actions
                        div(classes = "flex items-center justify-between pt-4") {
                            a(
                                href = "/admin/dashboard",
                                classes = "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white",
                            ) {
                                +"Cancel"
                            }
                            button(type = ButtonType.submit, classes = "btn-primary") {
                                +"Import GEDCOM"
                            }
                        }
                    }
                }
            }
        }
}
