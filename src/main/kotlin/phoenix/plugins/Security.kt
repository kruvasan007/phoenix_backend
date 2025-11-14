package phoenix.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import phoenix.service.JwtService
import phoenix.service.UserService

fun Application.configureSecurity() {
    val jwtService = JwtService()
    val userService = UserService()

    authentication {
        jwt("auth-jwt") {
            realm = "Phoenix Backend"
            verifier(JwtService.verifier)
            validate { credential ->
                // Проверяем, что токен валиден через стандартную проверку JWT
                val userId = credential.payload.getClaim("userId")?.asInt()
                if (userId != null) {
                    // Проверяем, что пользователь все еще активен
                    val user = userService.getUserById(userId)
                    if (user != null && user.isActive) {
                        JWTPrincipal(credential.payload)
                    } else null
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token"))
            }
        }
    }
}
