package phoenix.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
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
                    isLenient = true
                })
            }
        }
        println("[PolymModelService] Sending report for prediction: $report")
        val requestData = PredictionRequest(
            state = "Отличное",
            screen = report.screen,
            body = report.body,
            vendor = report.deviceId?.split(" ")?.getOrNull(0) ?: "Неизвестно",
            memoryFast = report.ram.toInt(),
            memoryPermanent = report.totalSpace.toInt()
        )

        return@runBlocking try {
            val httpResponse: HttpResponse = client.post("http://localhost:5003/predict") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            val raw = httpResponse.bodyAsText()
            println("[PolymModelService] Raw response: $raw")
            // Пытаемся десериализовать
            val response: PredictionResponse = try {
                httpResponse.body()
            } catch (e: Exception) {
                println("[PolymModelService] Deserialization error: ${e.message}")
                // Падает если ключ отсутствует. Возвращаем null.
                return@runBlocking null
            }
            val price = response.resolvedPrice()
            if (price == null) {
                println("[PolymModelService] 'predictedPrice' или альтернативный ключ отсутствует в ответе")
            }
            price
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
    @SerialName("predictedPrice") val predictedPrice: Double? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("predicted_price") val predictedPriceSnake: Double? = null
) {
    fun resolvedPrice(): Double? = predictedPrice ?: price ?: predictedPriceSnake
}
