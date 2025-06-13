package phoenix.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import phoenix.models.Report

class PolymModelService {
    fun getPredictedPrice(report: Report): Double? = runBlocking {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }
        }
        println(report)
        val requestData = PredictionRequest(
            state = "Отличное",
            screen = report.screen,
            body = report.body,
            vendor = report.deviceId?.split(" ")?.get(0) ?: "Неизвестно",
            memoryFast = report.ram.toInt(),
            memoryPermanent = report.totalSpace.toInt()
        )

        return@runBlocking try {
            val response: PredictionResponse =
                client.post("http://localhost:5003/predict") {
                    contentType(ContentType.Application.Json)
                    setBody(requestData)
                }.body()

            response.predictedPrice

        } catch (e: Exception) {
            println("Ошибка при запросе: ${e.message}")
            null
        } finally {
            client.close()
        }
    }
}

@Serializable
data class PredictionRequest(
    val state: String,
    val screen: String,
    val body: String,
    val vendor: String,
    val memoryFast: Int,
    val memoryPermanent: Int
)

@Serializable
data class PredictionResponse(
    val predictedPrice: Double
)