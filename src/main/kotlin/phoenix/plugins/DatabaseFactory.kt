package phoenix.plugins

import org.jetbrains.exposed.sql.Database
import java.lang.Thread.sleep

object DatabaseFactory {
    fun init() {
        // Используем DATABASE_URL если есть, иначе собираем из отдельных переменных
        val url = System.getenv("DATABASE_URL") ?: run {
            val host = System.getenv("DB_HOST") ?: "localhost"
            val port = System.getenv("DB_PORT") ?: "5432"
            val dbName = System.getenv("DB_NAME") ?: "phoenixback"
            "jdbc:postgresql://$host:$port/$dbName"
        }

        val user = System.getenv("DATABASE_USER") ?: System.getenv("DB_USER") ?: "admin"
        val password = System.getenv("DATABASE_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: "10Nhtqcth"

        // Retry логика, чтобы приложение корректно стартовало даже если Postgres ещё не готов
        val maxAttempts = (System.getenv("DB_CONNECT_ATTEMPTS") ?: "10").toInt()
        val delayMs = (System.getenv("DB_CONNECT_DELAY_MS") ?: "2000").toLong()
        var attempt = 1
        var lastError: Throwable? = null
        while (attempt <= maxAttempts) {
            try {
                Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password
                )
                println("[DatabaseFactory] Connected to Postgres at $url (attempt $attempt/$maxAttempts)")
                return
            } catch (e: Throwable) {
                lastError = e
                println("[DatabaseFactory] Connection attempt $attempt failed: ${e.message}. Retrying in ${delayMs}ms...")
                sleep(delayMs)
                attempt++
            }
        }
        println("[DatabaseFactory] Failed to connect after $maxAttempts attempts. Last error: ${lastError?.message}")
        throw lastError ?: IllegalStateException("Unknown DB connection error")
    }
}