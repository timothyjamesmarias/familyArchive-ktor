package com.timothymarias.familyarchive.routes

import com.timothymarias.familyarchive.config.redirectWithFlash
import com.timothymarias.familyarchive.model.ArtifactType
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.service.ArtifactFileService
import com.timothymarias.familyarchive.service.ArtifactService
import com.timothymarias.familyarchive.service.ArtifactUploadService
import com.timothymarias.familyarchive.service.ArticleService
import com.timothymarias.familyarchive.service.FamilyMemberService
import com.timothymarias.familyarchive.service.FamilyService
import com.timothymarias.familyarchive.service.FileUpload
import com.timothymarias.familyarchive.service.IndividualEventService
import com.timothymarias.familyarchive.service.IndividualService
import com.timothymarias.familyarchive.service.PlaceService
import com.timothymarias.familyarchive.service.UserService
import com.timothymarias.familyarchive.views.AdminViewContext
import com.timothymarias.familyarchive.views.adminViewContext
import com.timothymarias.familyarchive.views.admin.AdminArticlesViews
import com.timothymarias.familyarchive.views.admin.AdminArtifactsViews
import com.timothymarias.familyarchive.views.admin.AdminDashboardViews
import com.timothymarias.familyarchive.views.admin.AdminPlacesViews
import com.timothymarias.familyarchive.views.admin.AdminUsersViews
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDateTime

fun Route.adminRoutes(isDevMode: Boolean, tinymceApiKey: String) {
    val userService by inject<UserService>()
    val individualService by inject<IndividualService>()
    val familyService by inject<FamilyService>()
    val familyMemberService by inject<FamilyMemberService>()
    val placeService by inject<PlaceService>()
    val individualEventService by inject<IndividualEventService>()
    val artifactService by inject<ArtifactService>()
    val artifactUploadService by inject<ArtifactUploadService>()
    val artifactFileService by inject<ArtifactFileService>()
    val articleService by inject<ArticleService>()

    route("/admin") {
        // Dashboard
        get("/dashboard") {
            val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
            val userCount = userService.count()
            call.respondText(AdminDashboardViews.dashboard(ctx, userCount), ContentType.Text.Html)
        }

        // ---- Users ----
        route("/users") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val users = userService.findAll(PageRequest(page, size))
                call.respondText(AdminUsersViews.index(ctx, users), ContentType.Text.Html)
            }

            get("/new") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminUsersViews.`new`(ctx), ContentType.Text.Html)
            }

            post {
                val params = call.receiveParameters()
                try {
                    userService.create(
                        email = params["email"] ?: "",
                        password = params["password"] ?: "",
                        name = params["name"] ?: "",
                    )
                    call.redirectWithFlash("/admin/users", "success", "User created successfully")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/users/new", "error", e.message ?: "Failed to create user")
                }
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/users")
                val user = userService.findById(id) ?: return@get call.respondRedirect("/admin/users")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminUsersViews.edit(ctx, user), ContentType.Text.Html)
            }

            post("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/users")
                val params = call.receiveParameters()
                try {
                    userService.update(
                        id = id,
                        email = params["email"] ?: "",
                        name = params["name"] ?: "",
                        newPassword = params["password"]?.takeIf { it.isNotBlank() },
                    )
                    call.redirectWithFlash("/admin/users", "success", "User updated successfully")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/users/$id/edit", "error", e.message ?: "Failed to update user")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/users")
                userService.delete(id)
                call.redirectWithFlash("/admin/users", "success", "User deleted")
            }
        }

        // ---- Articles ----
        route("/articles") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val articles = articleService.findAll(PageRequest(page, size))
                call.respondText(AdminArticlesViews.index(ctx, articles), ContentType.Text.Html)
            }

            get("/new") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminArticlesViews.newForm(ctx), ContentType.Text.Html)
            }

            post {
                val params = call.receiveParameters()
                try {
                    val slug = params["slug"] ?: ""
                    val title = params["title"] ?: ""
                    val excerpt = params["excerpt"]
                    val content = params["content"] ?: ""
                    articleService.create(slug, title, excerpt, content, null)
                    call.redirectWithFlash("/admin/articles", "success", "Article created")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/articles/new", "error", e.message ?: "Failed to create article")
                }
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/articles")
                val article = articleService.findById(id) ?: return@get call.respondRedirect("/admin/articles")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminArticlesViews.edit(ctx, article), ContentType.Text.Html)
            }

            post("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/articles")
                val params = call.receiveParameters()
                try {
                    articleService.update(
                        id = id,
                        slug = params["slug"] ?: "",
                        title = params["title"] ?: "",
                        excerpt = params["excerpt"],
                        content = params["content"] ?: "",
                        publishedAt = null, // Preserve existing
                    )
                    call.redirectWithFlash("/admin/articles/$id/edit", "success", "Article updated")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/articles/$id/edit", "error", e.message ?: "Failed to update")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/articles")
                articleService.delete(id)
                call.redirectWithFlash("/admin/articles", "success", "Article deleted")
            }

            post("/{id}/publish") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/articles")
                articleService.publish(id)
                call.redirectWithFlash("/admin/articles/$id/edit", "success", "Article published")
            }

            post("/{id}/unpublish") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/articles")
                articleService.unpublish(id)
                call.redirectWithFlash("/admin/articles/$id/edit", "success", "Article unpublished")
            }
        }

        // ---- Places ----
        route("/places") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val search = call.parameters["search"]
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val places = if (!search.isNullOrBlank()) {
                    placeService.searchByAnyField(search, PageRequest(page, size))
                } else {
                    placeService.findAll(PageRequest(page, size))
                }
                call.respondText(AdminPlacesViews.index(ctx, places, search), ContentType.Text.Html)
            }

            get("/new") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminPlacesViews.`new`(ctx), ContentType.Text.Html)
            }

            post {
                val params = call.receiveParameters()
                try {
                    placeService.create(
                        name = params["name"] ?: "",
                        normalizedName = params["name"]?.trim(),
                        city = params["city"],
                        stateProvince = params["stateProvince"],
                        country = params["country"],
                        latitude = params["latitude"]?.toBigDecimalOrNull(),
                        longitude = params["longitude"]?.toBigDecimalOrNull(),
                    )
                    call.redirectWithFlash("/admin/places", "success", "Place created")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/places/new", "error", e.message ?: "Failed to create place")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/places")
                val place = placeService.findById(id) ?: return@get call.respondRedirect("/admin/places")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminPlacesViews.show(ctx, place), ContentType.Text.Html)
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/places")
                val place = placeService.findById(id) ?: return@get call.respondRedirect("/admin/places")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminPlacesViews.edit(ctx, place), ContentType.Text.Html)
            }

            post("/{id}/update") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/places")
                val params = call.receiveParameters()
                try {
                    placeService.update(
                        id = id,
                        name = params["name"] ?: "",
                        normalizedName = params["name"]?.trim(),
                        city = params["city"],
                        stateProvince = params["stateProvince"],
                        country = params["country"],
                        latitude = params["latitude"]?.toBigDecimalOrNull(),
                        longitude = params["longitude"]?.toBigDecimalOrNull(),
                    )
                    call.redirectWithFlash("/admin/places/$id", "success", "Place updated")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/places/$id/edit", "error", e.message ?: "Failed to update")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/places")
                placeService.delete(id)
                call.redirectWithFlash("/admin/places", "success", "Place deleted")
            }
        }

        // ---- Artifacts ----
        route("/artifacts") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val artifacts = artifactService.findAll(PageRequest(page, size))
                call.respondText(AdminArtifactsViews.index(ctx, artifacts), ContentType.Text.Html)
            }

            get("/upload") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(AdminArtifactsViews.uploadForm(ctx, ArtifactType.entries), ContentType.Text.Html)
            }

            post("/upload") {
                val multipart = call.receiveMultipart()
                val files = mutableListOf<FileUpload>()
                var artifactType = ArtifactType.DOCUMENT
                var title: String? = null
                var originalDateString: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "artifactType" -> artifactType = ArtifactType.fromString(part.value)
                                "title" -> title = part.value.takeIf { it.isNotBlank() }
                                "originalDateString" -> originalDateString = part.value.takeIf { it.isNotBlank() }
                            }
                        }
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName ?: "file"
                            val channel = part.provider()
                            val bytes = channel.readRemaining().readByteArray()
                            files.add(
                                FileUpload(
                                    inputStream = bytes.inputStream(),
                                    fileName = fileName,
                                    contentType = part.contentType?.toString() ?: "application/octet-stream",
                                    fileSize = bytes.size.toLong(),
                                ),
                            )
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                try {
                    val id = artifactUploadService.uploadArtifact(files, artifactType, title, originalDateString)
                    call.redirectWithFlash("/admin/artifacts/$id/edit", "success", "Artifact uploaded")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/artifacts/upload", "error", e.message ?: "Upload failed")
                }
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/artifacts")
                val artifact = artifactService.findById(id) ?: return@get call.respondRedirect("/admin/artifacts")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(
                    AdminArtifactsViews.edit(ctx, artifact, ArtifactType.entries),
                    ContentType.Text.Html,
                )
            }

            post("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/artifacts")
                val params = call.receiveParameters()
                try {
                    artifactService.update(
                        id = id,
                        title = params["title"],
                        artifactType = ArtifactType.fromString(params["artifactType"] ?: "DOCUMENT"),
                        originalDateString = params["originalDateString"],
                    )
                    call.redirectWithFlash("/admin/artifacts/$id/edit", "success", "Artifact updated")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/artifacts/$id/edit", "error", e.message ?: "Update failed")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/artifacts")
                artifactService.delete(id)
                call.redirectWithFlash("/admin/artifacts", "success", "Artifact deleted")
            }

            post("/{id}/files/{fileId}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/artifacts")
                val fileId = call.parameters["fileId"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/artifacts/$id/edit")
                try {
                    artifactService.deleteArtifactFile(id, fileId)
                    call.redirectWithFlash("/admin/artifacts/$id/edit", "success", "File deleted")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/artifacts/$id/edit", "error", e.message ?: "Failed to delete file")
                }
            }
        }

        // ---- Individuals ----
        route("/individuals") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val search = call.parameters["search"]
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val individuals = if (!search.isNullOrBlank()) {
                    individualService.searchByNameOrGedcomId(search, PageRequest(page, size))
                } else {
                    individualService.findAll(PageRequest(page, size))
                }
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminIndividualsViews.index(ctx, individuals, search),
                    ContentType.Text.Html,
                )
            }

            get("/new") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val allIndividuals = individualService.findAll(PageRequest(0, 10000)).content
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminIndividualsViews.`new`(ctx, allIndividuals),
                    ContentType.Text.Html,
                )
            }

            post {
                val params = call.receiveParameters()
                try {
                    individualService.createFromRequest(
                        com.timothymarias.familyarchive.service.IndividualRequest(
                            givenName = params["givenName"],
                            surname = params["surname"],
                            sex = params["sex"],
                            birthDate = params["birthDate"],
                            birthPlace = params["birthPlace"],
                            deathDate = params["deathDate"],
                            deathPlace = params["deathPlace"],
                        ),
                    )
                    call.redirectWithFlash("/admin/individuals", "success", "Individual created")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/individuals/new", "error", e.message ?: "Failed to create")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/individuals")
                val individual = individualService.findById(id) ?: return@get call.respondRedirect("/admin/individuals")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val events = individualEventService.findByIndividualIdOrderByDate(id).map { event ->
                    com.timothymarias.familyarchive.views.admin.EnrichedEvent(
                        event = event,
                        place = event.placeId?.let { placeService.findById(it) },
                    )
                }
                val memberships = familyMemberService.findByIndividualId(id).map { member ->
                    com.timothymarias.familyarchive.views.admin.EnrichedFamilyMembership(
                        member = member,
                        family = familyService.findById(member.familyId)!!,
                    )
                }
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminIndividualsViews.show(ctx, individual, events, memberships),
                    ContentType.Text.Html,
                )
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/individuals")
                val individual = individualService.findById(id) ?: return@get call.respondRedirect("/admin/individuals")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val allIndividuals = individualService.findAll(PageRequest(0, 10000)).content
                val dto = com.timothymarias.familyarchive.dto.IndividualUpdateDto(
                    gedcomId = individual.gedcomId,
                    givenName = individual.givenName,
                    surname = individual.surname,
                    sex = individual.sex?.toString(),
                    isTreeRoot = individual.isTreeRoot,
                )
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminIndividualsViews.edit(ctx, individual, allIndividuals, dto),
                    ContentType.Text.Html,
                )
            }

            post("/{id}/update") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/individuals")
                val params = call.receiveParameters()
                try {
                    individualService.updateFromRequest(
                        id,
                        com.timothymarias.familyarchive.service.IndividualRequest(
                            givenName = params["givenName"],
                            surname = params["surname"],
                            sex = params["sex"],
                            birthDate = params["birthDate"],
                            birthPlace = params["birthPlace"],
                            deathDate = params["deathDate"],
                            deathPlace = params["deathPlace"],
                        ),
                    )
                    call.redirectWithFlash("/admin/individuals/$id", "success", "Individual updated")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/individuals/$id/edit", "error", e.message ?: "Failed to update")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/individuals")
                try {
                    individualService.softDelete(id)
                    call.redirectWithFlash("/admin/individuals", "success", "Individual deleted")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/individuals/$id", "error", e.message ?: "Cannot delete")
                }
            }
        }

        // ---- Families ----
        route("/families") {
            get {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val size = call.parameters["size"]?.toIntOrNull() ?: 20
                val search = call.parameters["search"]
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val familyRecords = if (!search.isNullOrBlank()) {
                    familyService.searchByGedcomIdOrMembersOrPlace(search, PageRequest(page, size))
                } else {
                    familyService.findAll(PageRequest(page, size))
                }
                val familyRows = com.timothymarias.familyarchive.repository.Page(
                    content = familyRecords.content.map { family ->
                        val members = familyMemberService.findByFamilyId(family.id)
                        val placeName = family.marriagePlaceId?.let { placeService.findById(it)?.name }
                        com.timothymarias.familyarchive.views.admin.FamilyRowData(family, members.size, placeName)
                    },
                    totalElements = familyRecords.totalElements,
                    number = familyRecords.number,
                    size = familyRecords.size,
                )
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminFamiliesViews.index(ctx, familyRows, search),
                    ContentType.Text.Html,
                )
            }

            get("/new") {
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminFamiliesViews.newFamily(ctx),
                    ContentType.Text.Html,
                )
            }

            post {
                val params = call.receiveParameters()
                try {
                    familyService.create(
                        gedcomId = params["gedcomId"]?.takeIf { it.isNotBlank() },
                        marriageDateString = params["marriageDateString"]?.takeIf { it.isNotBlank() },
                        marriagePlaceId = params["marriagePlaceId"]?.toLongOrNull(),
                    )
                    call.redirectWithFlash("/admin/families", "success", "Family created")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/families/new", "error", e.message ?: "Failed to create")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/families")
                val family = familyService.findById(id) ?: return@get call.respondRedirect("/admin/families")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val members = familyMemberService.findByFamilyId(id).map { member ->
                    com.timothymarias.familyarchive.views.admin.EnrichedFamilyMember(
                        member = member,
                        individual = individualService.findById(member.individualId)!!,
                    )
                }
                val marriagePlace = family.marriagePlaceId?.let { placeService.findById(it) }
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminFamiliesViews.show(ctx, family, members, marriagePlace),
                    ContentType.Text.Html,
                )
            }

            get("/{id}/edit") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respondRedirect("/admin/families")
                val family = familyService.findById(id) ?: return@get call.respondRedirect("/admin/families")
                val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
                val places = placeService.findAll(PageRequest(0, 10000)).content
                val dto = com.timothymarias.familyarchive.dto.FamilyUpdateDto(
                    gedcomId = family.gedcomId,
                    marriageDateString = family.marriageDateString,
                    marriagePlaceId = family.marriagePlaceId,
                    divorceDateString = family.divorceDateString,
                )
                call.respondText(
                    com.timothymarias.familyarchive.views.admin.AdminFamiliesViews.edit(ctx, family, places, dto),
                    ContentType.Text.Html,
                )
            }

            post("/{id}/update") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/families")
                val params = call.receiveParameters()
                try {
                    familyService.update(
                        id = id,
                        marriageDateString = params["marriageDateString"]?.takeIf { it.isNotBlank() },
                        marriageDateParsed = null,
                        marriagePlaceId = params["marriagePlaceId"]?.toLongOrNull(),
                        divorceDateString = params["divorceDateString"]?.takeIf { it.isNotBlank() },
                        divorceDateParsed = null,
                    )
                    call.redirectWithFlash("/admin/families/$id", "success", "Family updated")
                } catch (e: Exception) {
                    call.redirectWithFlash("/admin/families/$id/edit", "error", e.message ?: "Failed to update")
                }
            }

            post("/{id}/delete") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respondRedirect("/admin/families")
                familyService.delete(id)
                call.redirectWithFlash("/admin/families", "success", "Family deleted")
            }
        }

        // ---- System Utilities ----
        get("/system/utilities") {
            val ctx = call.adminViewContext(isDevMode, tinymceApiKey)
            val stats = com.timothymarias.familyarchive.service.ThumbnailStats(0, 0, 0, 0, 0)
            call.respondText(
                com.timothymarias.familyarchive.views.admin.AdminSystemUtilitiesViews.utilities(ctx, stats),
                ContentType.Text.Html,
            )
        }
    }
}
