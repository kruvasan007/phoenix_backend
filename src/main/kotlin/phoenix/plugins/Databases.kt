package phoenix.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import phoenix.entity.ReportTable
import phoenix.entity.UserTable

fun Application.configureDatabase() {
    transaction {
        SchemaUtils.create(UserTable, ReportTable)
    }
}