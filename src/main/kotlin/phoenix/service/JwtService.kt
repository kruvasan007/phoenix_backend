package phoenix.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import phoenix.models.AuthTokenPayload
import phoenix.models.User
import java.util.*

class JwtService {
    companion object {
        private val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key"
        private val jwtIssuer = System.getenv("JWT_ISSUER") ?: "phoenix-app"
        private val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "phoenix-users"
        private const val TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L // 24 часа в миллисекундах

        private val algorithm = Algorithm.HMAC256(jwtSecret)
        val verifier: JWTVerifier = JWT
            .require(algorithm)
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .build()
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + TOKEN_EXPIRATION_TIME)

        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withSubject(user.id.toString())
            .withClaim("userId", user.id)
            .withClaim("username", user.username)
            .withClaim("email", user.email)
            .withIssuedAt(now)
            .withExpiresAt(expiry)
            .sign(algorithm)
    }

    fun validateToken(token: String): AuthTokenPayload? {
        return try {
            val jwt = verifier.verify(token)
            AuthTokenPayload(
                userId = jwt.getClaim("userId").asInt(),
                username = jwt.getClaim("username").asString(),
                email = jwt.getClaim("email").asString(),
                exp = jwt.expiresAt.time / 1000,
                iat = jwt.issuedAt.time / 1000
            )
        } catch (exception: JWTVerificationException) {
            null
        }
    }

    fun getTokenExpirationTime(): Long {
        return TOKEN_EXPIRATION_TIME / 1000 // возвращаем в секундах
    }
}
