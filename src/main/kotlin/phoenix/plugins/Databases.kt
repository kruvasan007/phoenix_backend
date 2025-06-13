package phoenix.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import phoenix.entity.ReportTable

fun Application.configureDatabase() {
    transaction {
        SchemaUtils.create(ReportTable)
    }
}