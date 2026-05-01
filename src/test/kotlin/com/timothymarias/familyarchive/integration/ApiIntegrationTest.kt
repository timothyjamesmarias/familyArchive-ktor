package com.timothymarias.familyarchive.integration

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiIntegrationTest : IntegrationTest() {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `health endpoint returns 200`() = integrationTest { client ->
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `family tree initial returns empty tree on fresh db`() = integrationTest { client ->
        val response = client.get("/api/family-tree/initial")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body.containsKey("individuals"))
        assertTrue(body.containsKey("families"))
        assertEquals(0, body["individuals"]!!.jsonArray.size)
        assertEquals(0, body["families"]!!.jsonArray.size)
    }

    @Test
    fun `individuals root returns empty array on fresh db`() = integrationTest { client ->
        val response = client.get("/api/individuals/root")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText())
        assertTrue(body is JsonArray)
        assertEquals(0, body.size)
    }

    @Test
    fun `individual not found returns 404`() = integrationTest { client ->
        val response = client.get("/api/individuals/99999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `family tree expand requires personId`() = integrationTest { client ->
        val response = client.get("/api/family-tree/expand")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `public pages return HTML`() = integrationTest { client ->
        val routes = listOf("/", "/login", "/family-tree", "/articles", "/artifacts")
        for (route in routes) {
            val response = client.get(route)
            assertEquals(HttpStatusCode.OK, response.status, "Route $route should return 200")
            assertTrue(response.bodyAsText().contains("<!DOCTYPE html>"), "Route $route should return HTML")
        }
    }

    @Test
    fun `admin routes redirect to login without auth`() = integrationTest { client ->
        val routes = listOf("/admin/dashboard", "/admin/users", "/admin/individuals", "/admin/families")
        for (route in routes) {
            val response = client.get(route)
            // testApplication follows redirects by default, so we get the login page
            assertEquals(HttpStatusCode.OK, response.status, "Route $route should resolve (after redirect)")
            assertTrue(response.bodyAsText().contains("Login"), "Route $route should show login page")
        }
    }

    @Test
    fun `creating an individual via API requires auth`() = integrationTest { client ->
        val response = client.post("/api/family-tree/individuals") {
            contentType(ContentType.Application.Json)
            setBody("""{"givenName": "John", "surname": "Doe", "sex": "M"}""")
        }
        // AuthGuard should block this
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
