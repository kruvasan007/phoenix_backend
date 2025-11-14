import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import phoenix.models.Report
import phoenix.models.ReportAnswer
import phoenix.repository.ReportRepository
import phoenix.service.GraphProxyService
import phoenix.service.PolymModelService

fun Route.reportRoutes(
    reportService: ReportRepository,
    service: PolymModelService
) {
    authenticate("auth-jwt") {
        route("/reports") {
        post {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                    return@post
                }

                val report = call.receive<Report>()
                val reportId = withContext(Dispatchers.IO) { reportService.createReport(report, userId) }
                val price = service.getPredictedPrice(report) ?: throw IllegalArgumentException("No price found")
                call.respond(
                    HttpStatusCode.OK, ReportAnswer(
                        report_id = reportId,
                        price = price,
                        mark = report.mark,
                        condition = "OK",
                        model = report.deviceId.toString(),
                        url = "http://example.com"
                    )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("Error" to e.localizedMessage))
            }
        }

        get {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                    return@get
                }

                val reports = withContext(Dispatchers.IO) { reportService.getAllReports(userId) }
                call.respond(HttpStatusCode.OK, reports)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to e.localizedMessage))
            }
        }

        get("{deviceId}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                    return@get
                }

                val deviceId = call.parameters["deviceId"]
                if (deviceId.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Missing or invalid 'deviceId'."))
                    return@get
                }
                val report = withContext(Dispatchers.IO) { reportService.getReportByDeviceId(deviceId, userId) }
                if (report != null) {
                    call.respond(HttpStatusCode.OK, report)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf<String, String>("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to e.localizedMessage))
            }
        }

        put("{deviceId}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                    return@put
                }

                val deviceId = call.parameters["deviceId"]
                if (deviceId.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Missing or invalid 'deviceId'."))
                    return@put
                }
                val report = call.receive<Report>()
                val isUpdated = reportService.updateReport(deviceId, report, userId)
                if (isUpdated) {
                    call.respond(HttpStatusCode.OK, mapOf<String, String>("status" to "Report updated successfully."))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf<String, String>("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to e.localizedMessage))
            }
        }

        delete("{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                    return@delete
                }

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Missing or invalid 'id'."))
                    return@delete
                }
                val isDeleted = withContext(Dispatchers.IO) { reportService.deleteReportById(id, userId) }
                if (isDeleted) {
                    call.respond(HttpStatusCode.OK, mapOf<String, String>("status" to "Report deleted successfully."))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf<String, String>("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to e.localizedMessage))
            }
        }
        }
    }
}

fun Route.graphProxyRoutes(graphService: GraphProxyService, reportService: ReportRepository) {
    authenticate("auth-jwt") {
        delete("/clear") {
            val result = graphService.clearGraph()
            call.respondText(result.body, result.contentType, result.status)
        }
        post("/ingest") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asInt()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                return@post
            }

            val payload = try {
                call.receive<Map<String, String>>()
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Invalid JSON"))
                return@post
            }
            val question = payload["question"]
            if (question.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Field 'question' is required."))
                return@post
            }

            try {
                // Получаем последний отчет пользователя
                val latestReport = withContext(Dispatchers.IO) { reportService.getLatestReport(userId) }

                val result = if (latestReport != null) {
                    // Если есть отчет, передаем его характеристики
                    graphService.ingestWithReport(question, latestReport)
                } else {
                    // Если отчета нет, отправляем только вопрос
                    graphService.ingest(question)
                }

                call.respondText(result.body, result.contentType, result.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to e.localizedMessage))
            }
        }
    }
}
