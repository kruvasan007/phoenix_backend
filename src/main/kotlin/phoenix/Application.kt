package phoenix

import graphProxyRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.tomcat.*
import phoenix.plugins.*
import phoenix.repository.ReportRepository
import phoenix.service.GraphProxyService
import phoenix.service.JwtService
import phoenix.service.PolymModelService
import phoenix.service.UserService
import reportRoutes

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    val reportService = ReportRepository()
    val service = PolymModelService()
    val graphService = GraphProxyService()
    val userService = UserService()
    val jwtService = JwtService()

    configureDatabase()
    configureSerialization()
    configureHTTP()
    configureSecurity()

    routing {
        authRoutes(userService, jwtService)
        reportRoutes(reportService, service)
        graphProxyRoutes(graphService, reportService)
    }
}
