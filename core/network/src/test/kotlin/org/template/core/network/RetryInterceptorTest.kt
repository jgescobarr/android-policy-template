package org.template.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RetryInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @BeforeEach
    fun setup() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder().addInterceptor(RetryInterceptor()).build()
    }

    @AfterEach
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `reintenta ante un 503 y retorna éxito en el segundo intento`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val request = Request.Builder().url(server.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `no reintenta ante un error 4xx no transitorio`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val request = Request.Builder().url(server.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(404, response.code)
        assertEquals(1, server.requestCount)
    }
}
