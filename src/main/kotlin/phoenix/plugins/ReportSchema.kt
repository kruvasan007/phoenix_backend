package phoenix.plugins

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

data class Report(
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("frequency")
    var frequency: String,
    @SerializedName("mark")
    var mark: String,
    @SerializedName("width")
    var width: Int,
    @SerializedName("height")
    var height: Int,
    @SerializedName("density")
    var density: Float,
    @SerializedName("ram")
    var ram: Long,
    @SerializedName("totalSpace")
    var totalSpace: Long,
    @SerializedName("gyroscope")
    var gyroscope: String? = "no gyro",
    @SerializedName("versionOS")
    var versionOS: String? = null,
    @SerializedName("batteryState")
    var batteryState: Int,
    @SerializedName("level")
    var level: Int,
    @SerializedName("dataStatus")
    var dataStatus: Int,
    @SerializedName("GPS")
    var GPS: Boolean,
    @SerializedName("bluetooth")
    var bluetooth: Boolean,
    @SerializedName("audioReport")
    val audioReport: Boolean
)

@Serializable
data class PriceAnswer(
    val deviceId: String? = null,
    val price: Int
)

class ReportService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_REPORT =
            "CREATE TABLE REPORTS (ID SERIAL PRIMARY KEY," +
                    " DEVICE_ID VARCHAR(255)," +
                    " FREQUENCY VARCHAR(255)," +
                    " MARK VARCHAR(255), " +
                    " WIDTH INT, " +
                    " HEIGHT INT, " +
                    " DENSITY FLOAT, " +
                    " RAM INT8," +
                    " TOTAL_SPACE INT8," +
                    " GYROSCOPE VARCHAR(255)," +
                    " VERSION_OS VARCHAR(255)," +
                    " BATTERY_STATE INT," +
                    " LEVEL INT," +
                    " DATA_STATUS INT," +
                    " GPS BOOL," +
                    " BLUETOOTH BOOL, " +
                    " AUDIO_REPORT BOOL, " +
                    " PRICE INT " +
                    ");"
        private const val SELECT_REPORT_BY_DEVICE_ID = "SELECT price FROM reports WHERE device_id = ?"
        private const val INSERT_REPORT =
            "INSERT INTO REPORTS (device_id, frequency, mark, width, height, density, ram," +
                    " total_space, gyroscope, version_os, battery_state, level, data_status, GPS," +
                    " bluetooth, audio_report, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        private const val UPDATE_REPORT = "UPDATE cities SET name = ?, population = ? WHERE device_id = ?"
        private const val DELETE_REPORT = "DELETE FROM report WHERE id = ?"
    }

    init {
        val statement = connection.createStatement()
        //statement.executeUpdate(CREATE_TABLE_REPORT)
    }

    private var newReportId = 0

    // Create new report
    suspend fun create(report: Report): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_REPORT, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, report.deviceId)

        statement.setString(2, report.frequency)
        statement.setString(3, report.mark)

        statement.setInt(4, report.width)
        statement.setInt(5, report.height)
        statement.setFloat(6, report.density)

        statement.setLong(7, report.ram)
        statement.setLong(8, report.totalSpace)

        statement.setString(9, report.gyroscope)
        statement.setString(10, report.versionOS)

        statement.setInt(11, report.batteryState)
        statement.setInt(12, report.level)
        statement.setInt(13, report.dataStatus)
        statement.setBoolean(14, report.GPS)
        statement.setBoolean(15, report.bluetooth)
        statement.setBoolean(16, report.audioReport)
        statement.setInt(17, 2)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    suspend fun getPrice(id: String): PriceAnswer = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_REPORT_BY_DEVICE_ID)
        statement.setString(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val price = resultSet.getInt("price")
            return@withContext PriceAnswer(id, price)
        } else {
            throw Exception("Record not found")
        }
    }

    /*
    // Update a city
    suspend fun update(id: Int, city: City) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CITY)
        statement.setString(1, city.name)
        statement.setInt(2, city.population)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CITY)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
     */
}

