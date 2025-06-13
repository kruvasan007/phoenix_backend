package phoenix

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.tomcat.*
import phoenix.plugins.*
import phoenix.repository.ReportRepository
import phoenix.service.PolymModelService
import reportRoutes

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    val reportService = ReportRepository()
    val service = PolymModelService()
    configureDatabase()
    configureSerialization()
    configureHTTP()
    configureSecurity()

    routing {
        reportRoutes(reportService, service)
    }
}
