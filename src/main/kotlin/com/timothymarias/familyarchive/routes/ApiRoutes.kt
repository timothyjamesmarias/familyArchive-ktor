package com.timothymarias.familyarchive.routes

import com.timothymarias.familyarchive.service.AnnotationInput
import com.timothymarias.familyarchive.service.AnnotationService
import com.timothymarias.familyarchive.service.FamilyTreeService
import com.timothymarias.familyarchive.service.IndividualService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.math.BigDecimal

fun Route.apiRoutes() {
    val familyTreeService by inject<FamilyTreeService>()
    val individualService by inject<IndividualService>()
    val annotationService by inject<AnnotationService>()

    route("/api") {
        // Family tree read endpoints (public)
        route("/family-tree") {
            get("/initial") {
                val tree = familyTreeService.getInitialTree()
                call.respond(tree)
            }

            get("/expand") {
                val personId = call.parameters["personId"]?.toLongOrNull()
                    ?: return@get call.respondText("personId required", status = HttpStatusCode.BadRequest)
                val generationsUp = call.parameters["generationsUp"]?.toIntOrNull() ?: 0
                val generationsDown = call.parameters["generationsDown"]?.toIntOrNull() ?: 0
                val includeSiblings = call.parameters["includeSiblings"]?.toBoolean() ?: false

                val tree = familyTreeService.expandTree(personId, generationsUp, generationsDown, includeSiblings)
                call.respond(tree)
            }

            // Mutation endpoints (auth enforced by AuthGuard)
            post("/individuals") {
                val request = call.receive<IndividualApiRequest>()
                val id = individualService.createFromRequest(
                    com.timothymarias.familyarchive.service.IndividualRequest(
                        givenName = request.givenName,
                        surname = request.surname,
                        sex = request.sex,
                        birthDate = request.birthDate,
                        birthPlace = request.birthPlace,
                        deathDate = request.deathDate,
                        deathPlace = request.deathPlace,
                    ),
                )
                val individual = individualService.findById(id)
                if (individual != null) {
                    call.respond(HttpStatusCode.Created, familyTreeService.toIndividualResponse(individual))
                } else {
                    call.respond(HttpStatusCode.Created, mapOf("id" to id))
                }
            }

            put("/individuals/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respondText("Invalid id", status = HttpStatusCode.BadRequest)
                val request = call.receive<IndividualApiRequest>()
                individualService.updateFromRequest(
                    id,
                    com.timothymarias.familyarchive.service.IndividualRequest(
                        givenName = request.givenName,
                        surname = request.surname,
                        sex = request.sex,
                        birthDate = request.birthDate,
                        birthPlace = request.birthPlace,
                        deathDate = request.deathDate,
                        deathPlace = request.deathPlace,
                    ),
                )
                val individual = individualService.findById(id)
                if (individual != null) {
                    call.respond(familyTreeService.toIndividualResponse(individual))
                } else {
                    call.respond(mapOf("id" to id))
                }
            }

            delete("/individuals/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respondText("Invalid id", status = HttpStatusCode.BadRequest)
                individualService.softDelete(id)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/individuals/{id}/can-delete") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondText("Invalid id", status = HttpStatusCode.BadRequest)
                val result = individualService.canDelete(id)
                call.respond(mapOf("valid" to result.valid, "reason" to result.reason))
            }

            // Relationship endpoints will be added when FamilyTreeRelationshipService is ported (Phase 8)
        }

        // Individual endpoints
        route("/individuals") {
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondText("Invalid id", status = HttpStatusCode.BadRequest)
                val individual = individualService.findById(id)
                if (individual == null) {
                    call.respondText("Not found", status = HttpStatusCode.NotFound)
                } else {
                    call.respond(familyTreeService.toIndividualResponse(individual))
                }
            }

            get("/root") {
                val roots = familyTreeService.getRootIndividuals()
                call.respond(roots.map { familyTreeService.toIndividualResponse(it) })
            }
        }

        // Annotation endpoints
        route("/artifact-files") {
            put("/{fileId}/annotations") {
                val fileId = call.parameters["fileId"]?.toLongOrNull()
                    ?: return@put call.respondText("Invalid fileId", status = HttpStatusCode.BadRequest)
                val request = call.receive<ReplaceAnnotationsRequest>()
                val inputs = request.annotations.map {
                    AnnotationInput(
                        id = it.id,
                        text = it.annotationText,
                        xCoord = it.xCoord?.let { x -> BigDecimal(x) },
                        yCoord = it.yCoord?.let { y -> BigDecimal(y) },
                    )
                }
                annotationService.replaceAnnotations(fileId, inputs)
                val updated = annotationService.findByArtifactFileId(fileId)
                call.respond(
                    ReplaceAnnotationsResponse(
                        annotations = updated.map {
                            AnnotationDto(it.id, it.annotationText, it.xCoord?.toDouble(), it.yCoord?.toDouble())
                        },
                    ),
                )
            }
        }
    }
}

@Serializable
data class IndividualApiRequest(
    val givenName: String? = null,
    val surname: String? = null,
    val sex: String? = null,
    val birthDate: String? = null,
    val birthPlace: String? = null,
    val deathDate: String? = null,
    val deathPlace: String? = null,
)

@Serializable
data class ReplaceAnnotationsRequest(
    val annotations: List<AnnotationDto>,
)

@Serializable
data class ReplaceAnnotationsResponse(
    val annotations: List<AnnotationDto>,
)

@Serializable
data class AnnotationDto(
    val id: Long? = null,
    val annotationText: String,
    val xCoord: Double? = null,
    val yCoord: Double? = null,
)
