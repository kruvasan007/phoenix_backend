package phoenix.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import phoenix.models.*
import phoenix.service.JwtService
import phoenix.service.UserService

fun Route.authRoutes(userService: UserService, jwtService: JwtService) {

    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<UserRegistrationRequest>()

                // Валидация
                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Username, email and password are required"))
                    return@post
                }

                if (request.password.length < 6) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Password must be at least 6 characters"))
                    return@post
                }

                if (!isValidEmail(request.email)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Invalid email format"))
                    return@post
                }

                val user = userService.createUser(request)
                if (user != null) {
                    val token = jwtService.generateToken(user)
                    call.respond(HttpStatusCode.Created, UserLoginResponse(
                        user = user,
                        token = token,
                        expiresIn = jwtService.getTokenExpirationTime()
                    ))
                } else {
                    call.respond(HttpStatusCode.Conflict, mapOf<String, String>("error" to "User with this username or email already exists"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Invalid request: ${e.localizedMessage}"))
            }
        }

        post("/login") {
            try {
                val request = call.receive<UserLoginRequest>()

                if (request.username.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Username and password are required"))
                    return@post
                }

                val user = userService.authenticateUser(request.username, request.password)
                if (user != null) {
                    val token = jwtService.generateToken(user)
                    call.respond(HttpStatusCode.OK, UserLoginResponse(
                        user = user,
                        token = token,
                        expiresIn = jwtService.getTokenExpirationTime()
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid username or password"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf<String, String>("error" to "Invalid request: ${e.localizedMessage}"))
            }
        }

        // Защищенный маршрут для получения информации о текущем пользователе
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()

                if (userId != null) {
                    val user = userService.getUserById(userId)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf<String, String>("error" to "User not found"))
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf<String, String>("error" to "Invalid token"))
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".")
}
