package phoenix.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UserTable : IntIdTable("users") {
    val username: Column<String> = varchar("username", 50).uniqueIndex()
    val email: Column<String> = varchar("email", 100).uniqueIndex()
    val passwordHash: Column<String> = varchar("password_hash", 255)
    val fullName: Column<String?> = varchar("full_name", 100).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
    val isActive: Column<Boolean> = bool("is_active").default(true)
}
