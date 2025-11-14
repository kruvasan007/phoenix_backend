package phoenix.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import phoenix.models.Report

class GraphProxyService {
    private val baseUrl: String = System.getenv("PYTHON_API_URL") ?: "http://localhost:8000"
    // Extended timeout (ms) configurable via env var PYTHON_API_TIMEOUT, default 60s
    private val timeoutMs: Long = System.getenv("PYTHON_API_TIMEOUT")?.toLongOrNull()?.takeIf { it > 0 } ?: 60_000L

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        // Apply extended timeouts
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMs
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = timeoutMs
        }
        engine {
            endpoint {
                connectTimeout = 15_000
                requestTimeout = timeoutMs // fixed type
                keepAliveTime = 5_000L // ensure Long
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
            }
            maxConnectionsCount = 1000
        }
    }

    suspend fun clearGraph(): ProxyResult = try {
        val resp = client.delete("$baseUrl/clear")
        val ct = resp.headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) } ?: ContentType.Application.Json
        ProxyResult(resp.status, resp.bodyAsText(), ct)
    } catch (e: Exception) {
        ProxyResult(HttpStatusCode.ServiceUnavailable, "{\"error\":\"Upstream clear failed: ${e.message}\"}", ContentType.Application.Json)
    }

    suspend fun ingest(question: String, reportCharacteristics: JsonObject? = null): ProxyResult = try {
        val requestBody = if (reportCharacteristics != null) {
            IngestRequestWithReport(question, reportCharacteristics)
        } else {
            IngestRequest(question)
        }

        val resp = client.post("$baseUrl/ingest") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        val ct = resp.headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) } ?: ContentType.Application.Json
        ProxyResult(resp.status, resp.bodyAsText(), ct)
    } catch (e: Exception) {
        ProxyResult(HttpStatusCode.ServiceUnavailable, "{\"error\":\"Upstream ingest failed: ${e.message}\"}", ContentType.Application.Json)
    }

    /**
     * Конвертирует объект Report в JsonObject для передачи в Python API
     */
    private fun convertReportToCharacteristics(report: Report): JsonObject {
        return buildJsonObject {
            put("deviceId", report.deviceId ?: "")
            put("body", report.body)
            put("screen", report.screen)
            put("frequency", report.frequency)
            put("mark", report.mark)
            put("width", report.width)
            put("height", report.height)
            put("density", report.density)
            put("ram", report.ram)
            put("totalSpace", report.totalSpace)
            put("gyroscope", report.gyroscope ?: "no gyro")
            put("versionOS", report.versionOS ?: "")
            put("batteryState", report.batteryState)
            put("level", report.level)
            put("dataStatus", report.dataStatus)
            put("gps", report.gps)
            put("bluetooth", report.bluetooth)
            put("audioReport", report.audioReport)
        }
    }

    /**
     * Ingest с отчетом
     */
    suspend fun ingestWithReport(question: String, report: Report): ProxyResult {
        val characteristics = convertReportToCharacteristics(report)
        return ingest(question, characteristics)
    }
}

@Serializable
data class IngestRequest(val question: String)

@Serializable
data class IngestRequestWithReport(
    val question: String,
    val reportCharacteristics: JsonObject
)

data class ProxyResult(val status: HttpStatusCode, val body: String, val contentType: ContentType)
