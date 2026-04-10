package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Platform provides the engine (Android uses OkHttp/HttpURLConnection, iOS uses Darwin/NSURLSession)
expect fun platformHttpClientEngine(): HttpClientEngine

// Default argument means production code calls createHttpClient() unchanged.
// Tests pass a MockEngine to avoid network access.
fun createHttpClient(engine: HttpClientEngine = platformHttpClientEngine()): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
}
