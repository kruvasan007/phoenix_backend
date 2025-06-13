import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import phoenix.models.Report
import phoenix.models.ReportAnswer
import phoenix.repository.ReportRepository
import phoenix.service.PolymModelService

fun Route.reportRoutes(
    reportService: ReportRepository,
    service: PolymModelService
) {

    route("/reports") {
        post {
            try {
                val report = call.receive<Report>()
                val reportId = withContext(Dispatchers.IO) {
                    reportService.createReport(report)
                }
                val price = service.getPredictedPrice(report) ?: throw IllegalArgumentException("No price found")
                call.respond(
                    HttpStatusCode.OK, ReportAnswer(
                        report_id = reportId,
                        price = price.toDouble(),
                        mark = report.mark,
                        condition = "OK",
                        model = report.deviceId.toString(),
                        url = "http://example.com"
                    )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.localizedMessage))
            }
        }

        get {
            try {
                val reports = withContext(Dispatchers.IO) {
                    reportService.getAllReports()
                }
                call.respond(HttpStatusCode.OK, reports)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        get("{deviceId}") {
            try {
                val deviceId = call.parameters["deviceId"]
                if (deviceId.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid 'deviceId'."))
                    return@get
                }

                val report = withContext(Dispatchers.IO) {
                    reportService.getReportByDeviceId(deviceId)
                }
                if (report != null) {
                    call.respond(HttpStatusCode.OK, report)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        put("{deviceId}") {
            try {
                val deviceId = call.parameters["deviceId"]
                if (deviceId.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid 'deviceId'."))
                    return@put
                }

                val report = call.receive<Report>()
                val isUpdated = withContext(Dispatchers.IO) {
                    reportService.updateReport(deviceId, report)
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "Report updated successfully."))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid 'id'."))
                    return@delete
                }

                val isDeleted = withContext(Dispatchers.IO) {
                    reportService.deleteReportById(id)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "Report deleted successfully."))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }
    }
}