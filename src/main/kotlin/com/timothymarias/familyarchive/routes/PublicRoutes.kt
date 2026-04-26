package com.timothymarias.familyarchive.routes

import com.timothymarias.familyarchive.model.ArtifactType
import com.timothymarias.familyarchive.repository.PageRequest
import com.timothymarias.familyarchive.service.ArtifactService
import com.timothymarias.familyarchive.service.ArticleService
import com.timothymarias.familyarchive.service.FamilyTreeService
import com.timothymarias.familyarchive.service.storage.StorageService
import com.timothymarias.familyarchive.components.pageLayout
import com.timothymarias.familyarchive.views.CsrfInfo
import com.timothymarias.familyarchive.views.ViewContext
import com.timothymarias.familyarchive.views.renderHtml
import com.timothymarias.familyarchive.views.viewContext
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import com.timothymarias.familyarchive.views.public.ArtifactViews
import com.timothymarias.familyarchive.views.public.ArticlesViews
import com.timothymarias.familyarchive.views.public.FamilyTreeViews
import com.timothymarias.familyarchive.views.public.HomeViews
import com.timothymarias.familyarchive.views.public.LoginViews
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.publicRoutes(isDevMode: Boolean) {
    val artifactService by inject<ArtifactService>()
    val articleService by inject<ArticleService>()
    val familyTreeService by inject<FamilyTreeService>()
    val storageService by inject<StorageService>()

    get("/") {
        val ctx = call.viewContext(isDevMode)
        call.respondText(HomeViews.index(ctx), ContentType.Text.Html)
    }

    get("/login") {
        val error = call.parameters["error"]
        val ctx = call.viewContext(isDevMode)
        val errorMessage = if (error != null) "Invalid email or password" else null
        // Login page needs a CSRF token for the form, but user isn't authenticated yet
        call.respondText(LoginViews.login(ctx, CsrfInfo(""), errorMessage), ContentType.Text.Html)
    }

    get("/family-tree") {
        val ctx = call.viewContext(isDevMode)
        call.respondText(FamilyTreeViews.familyTree(ctx, ctx.auth != null), ContentType.Text.Html)
    }

    get("/articles") {
        val page = call.parameters["page"]?.toIntOrNull() ?: 0
        val articles = articleService.findAllPublished(PageRequest(page, 12))
        val ctx = call.viewContext(isDevMode)
        call.respondText(ArticlesViews.index(ctx, articles), ContentType.Text.Html)
    }

    get("/articles/{slug}") {
        val slug = call.parameters["slug"]!!
        val article = articleService.findBySlug(slug)
        if (article == null) {
            call.respondRedirect("/articles")
            return@get
        }
        val ctx = call.viewContext(isDevMode)
        call.respondText(ArticlesViews.show(ctx, article), ContentType.Text.Html)
    }

    // Artifact type routes (photos, videos, documents, letters, audio, ledgers)
    for (type in ArtifactType.entries.filter { it != ArtifactType.OTHER }) {
        get("/${type.routeSegment}") {
            val page = call.parameters["page"]?.toIntOrNull() ?: 0
            val artifacts = artifactService.findByType(type, PageRequest(page, 24))
            val ctx = call.viewContext(isDevMode)
            call.respondText(
                ArtifactViews.index(ctx, artifacts, type, type.pluralDisplayName, type.routeSegment, {}),
                ContentType.Text.Html,
            )
        }

        get("/${type.routeSegment}/{slug}") {
            val slug = call.parameters["slug"]!!
            val artifact = artifactService.findByTypeAndSlug(type, slug)
            if (artifact == null) {
                call.respondRedirect("/${type.routeSegment}")
                return@get
            }
            val ctx = call.viewContext(isDevMode)
            call.respondText(ArtifactViews.show(ctx, artifact), ContentType.Text.Html)
        }
    }

    // Artifacts grid page — render simple type listing
    get("/artifacts") {
        val ctx = call.viewContext(isDevMode)
        call.respondText(artifactTypeGridHtml(ctx), ContentType.Text.Html)
    }

    // File serving for local uploads
    get("/uploads/{path...}") {
        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
        try {
            val inputStream = storageService.retrieve(path)
            val contentType = when {
                path.endsWith(".jpg") || path.endsWith(".jpeg") -> ContentType.Image.JPEG
                path.endsWith(".png") -> ContentType.Image.PNG
                path.endsWith(".gif") -> ContentType.Image.GIF
                path.endsWith(".webp") -> ContentType("image", "webp")
                path.endsWith(".pdf") -> ContentType.Application.Pdf
                path.endsWith(".mp4") -> ContentType.Video.MP4
                path.endsWith(".mp3") -> ContentType("audio", "mpeg")
                else -> ContentType.Application.OctetStream
            }
            call.respondOutputStream(contentType) {
                inputStream.use { it.copyTo(this) }
            }
        } catch (e: Exception) {
            call.respondText("File not found", status = HttpStatusCode.NotFound)
        }
    }
}

private fun artifactTypeGridHtml(ctx: ViewContext): String =
    renderHtml {
        pageLayout(
            title = "Artifacts",
            isDevMode = ctx.isDevMode,
            auth = ctx.auth,
        ) {
            h1(classes = "text-3xl font-bold text-gray-900 dark:text-white mb-8") { +"Artifacts" }
            div(classes = "grid grid-cols-2 md:grid-cols-3 gap-4") {
                ArtifactType.entries.filter { it != ArtifactType.OTHER }.forEach { type ->
                    a(
                        href = "/${type.routeSegment}",
                        classes = "p-6 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-md transition-shadow text-center",
                    ) {
                        p(classes = "text-lg font-semibold text-gray-900 dark:text-white") {
                            +type.pluralDisplayName
                        }
                    }
                }
            }
        }
    }
