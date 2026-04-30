package com.timothymarias.familyarchive.golden

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Golden master tests for the API layer.
 *
 * These tests run against a live Ktor server.
 * Start the server before running: PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run
 *
 * Set KTOR_TEST_PORT env var to override the port (default: 8081).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiGoldenMasterTest {

    private val port = System.getenv("KTOR_TEST_PORT")?.toIntOrNull() ?: 8081
    private val baseUrl = "http://localhost:$port"
    private val client = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeAll
    fun checkServerRunning() {
        val reachable = try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/health"))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() == 200
        } catch (e: Exception) {
            false
        }
        assumeTrue(reachable, "Ktor server not running on port $port — skipping golden master tests")
    }

    private fun get(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .GET()
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    // =========================================================================
    // Health
    // =========================================================================

    @Test
    fun `health returns 200 OK`() {
        val response = get("/health")
        assertEquals(200, response.statusCode())
        assertEquals("OK", response.body())
    }

    // =========================================================================
    // Family Tree API — structure contracts
    // =========================================================================

    @Test
    fun `family-tree initial returns 200 with correct structure`() {
        val response = get("/api/family-tree/initial")
        assertEquals(200, response.statusCode())

        val body = json.parseToJsonElement(response.body()).jsonObject
        assertTrue(body.containsKey("individuals"), "Must have 'individuals'")
        assertTrue(body.containsKey("families"), "Must have 'families'")
        assertTrue(body["individuals"] is JsonArray)
        assertTrue(body["families"] is JsonArray)
    }

    @Test
    fun `family-tree expand requires personId`() {
        val response = get("/api/family-tree/expand")
        assertEquals(400, response.statusCode())
    }

    @Test
    fun `family-tree expand with valid params returns correct structure`() {
        val response = get("/api/family-tree/expand?personId=1&generationsUp=1&generationsDown=0&includeSiblings=false")
        assertEquals(200, response.statusCode())

        val body = json.parseToJsonElement(response.body()).jsonObject
        assertTrue(body.containsKey("individuals"))
        assertTrue(body.containsKey("families"))
    }

    @Test
    fun `family-tree individual has expected fields when data exists`() {
        val response = get("/api/family-tree/initial")
        assertEquals(200, response.statusCode())

        val body = json.parseToJsonElement(response.body()).jsonObject
        val individuals = body["individuals"]?.jsonArray ?: return

        if (individuals.isNotEmpty()) {
            val individual = individuals[0].jsonObject
            val requiredFields = setOf("id", "givenName", "surname", "sex", "isTreeRoot", "metadata")
            for (field in requiredFields) {
                assertTrue(individual.containsKey(field), "Individual missing field '$field'. Has: ${individual.keys}")
            }

            val metadata = individual["metadata"]!!.jsonObject
            assertTrue(metadata.containsKey("childFamilyIds"))
            assertTrue(metadata.containsKey("spouseFamilyIds"))
            assertTrue(metadata.containsKey("hasUnloadedAncestors"))
            assertTrue(metadata.containsKey("hasUnloadedDescendants"))
            assertTrue(metadata.containsKey("hasUnloadedSiblings"))
        }
    }

    @Test
    fun `family-tree family unit has expected fields when data exists`() {
        val response = get("/api/family-tree/initial")
        assertEquals(200, response.statusCode())

        val body = json.parseToJsonElement(response.body()).jsonObject
        val families = body["families"]?.jsonArray ?: return

        if (families.isNotEmpty()) {
            val family = families[0].jsonObject
            assertTrue(family.containsKey("familyId"))
            assertTrue(family.containsKey("parentIds"))
            assertTrue(family.containsKey("childIds"))
        }
    }

    // =========================================================================
    // Individuals API
    // =========================================================================

    @Test
    fun `individuals root returns 200 with array`() {
        val response = get("/api/individuals/root")
        assertEquals(200, response.statusCode())

        val body = json.parseToJsonElement(response.body())
        assertTrue(body is JsonArray)
    }

    @Test
    fun `individual not found returns 404`() {
        val response = get("/api/individuals/99999")
        assertEquals(404, response.statusCode())
    }

    // =========================================================================
    // Auth enforcement (unauthenticated)
    // =========================================================================

    @Test
    fun `admin dashboard redirects to login without auth`() {
        val noRedirectClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/admin/dashboard"))
            .GET()
            .build()
        val response = noRedirectClient.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(302, response.statusCode())
        assertTrue(response.headers().firstValue("location").orElse("").contains("/login"))
    }

    // =========================================================================
    // Public pages return HTML
    // =========================================================================

    @Test
    fun `public pages return 200 with HTML`() {
        val publicRoutes = listOf(
            "/",
            "/login",
            "/family-tree",
            "/articles",
            "/photos",
            "/videos",
            "/documents",
            "/letters",
            "/audio",
            "/ledgers",
            "/artifacts",
        )

        for (route in publicRoutes) {
            val response = get(route)
            assertEquals(200, response.statusCode(), "Route $route should return 200")
            assertTrue(
                response.body().contains("<!DOCTYPE html>", ignoreCase = true),
                "Route $route should return HTML",
            )
        }
    }
}
