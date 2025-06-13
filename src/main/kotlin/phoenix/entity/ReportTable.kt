package phoenix.entity

import org.jetbrains.exposed.sql.Table

object ReportTable : Table("reports") {
    val id = integer("id").autoIncrement()
    val deviceId = varchar("deviceId", 255).nullable()
    val frequency = varchar("frequency", 255)
    val mark = varchar("mark", 255)
    val screen = varchar("screen", 255)
    val body = varchar("body", 255)
    val width = integer("width")
    val height = integer("height")
    val density = float("density")
    val ram = long("ram")
    val totalSpace = long("totalSpace")
    val gyroscope = varchar("gyroscope", 255).nullable()
    val versionOS = varchar("versionOS", 255).nullable()
    val batteryState = integer("batteryState")
    val level = integer("level")
    val dataStatus = integer("dataStatus")
    val gps = bool("gps")
    val bluetooth = bool("bluetooth")
    val audioReport = bool("audioReport")

    override val primaryKey = PrimaryKey(id)
}