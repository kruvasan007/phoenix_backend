package phoenix.plugins

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        // Укажите параметры своей базы данных
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/phoenixback",
            driver = "org.postgresql.Driver",
            user = "admin",
            password = "10Nhtqcth"
        )
    }
}