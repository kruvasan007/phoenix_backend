package phoenix.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import phoenix.entity.ReportTable
import phoenix.models.Report

class ReportRepository {

    /**
     * Создает новый отчет в базе данных
     * @param report объект Report, который нужно сохранить
     * @param userId ID пользователя, создающего отчет
     * @return ID созданного отчета
     */
    fun createReport(report: Report, userId: Int): Int {
        return transaction {
            ReportTable.insert {
                it[ReportTable.userId] = userId
                it[deviceId] = report.deviceId
                it[frequency] = report.frequency
                it[mark] = report.mark
                it[screen] = report.screen
                it[body] = report.body
                it[width] = report.width
                it[height] = report.height
                it[density] = report.density
                it[ram] = report.ram
                it[totalSpace] = report.totalSpace
                it[gyroscope] = report.gyroscope
                it[versionOS] = report.versionOS
                it[batteryState] = report.batteryState
                it[level] = report.level
                it[dataStatus] = report.dataStatus
                it[gps] = report.gps
                it[bluetooth] = report.bluetooth
                it[audioReport] = report.audioReport
            } get ReportTable.id
        }
    }

    /**
     * Получение всех отчетов пользователя
     * @param userId ID пользователя
     * @return список объектов Report из базы данных
     */
    fun getAllReports(userId: Int): List<Report> {
        return transaction {
            ReportTable.select { ReportTable.userId eq userId }.map { rowToReport(it) }
        }
    }

    /**
     * Получение отчета по `deviceId` и userId
     * @param deviceId ID устройства
     * @param userId ID пользователя
     * @return объект Report или null, если отчет не найден
     */
    fun getReportByDeviceId(deviceId: String, userId: Int): Report? {
        return transaction {
            ReportTable.select { (ReportTable.deviceId eq deviceId) and (ReportTable.userId eq userId) }
                .mapNotNull { rowToReport(it) }
                .singleOrNull()
        }
    }

    /**
     * Обновление отчета по `deviceId` и userId
     * @param deviceId ID устройства
     * @param report новые данные отчета
     * @param userId ID пользователя
     * @return true, если обновление было успешным, иначе false
     */
    fun updateReport(deviceId: String, report: Report, userId: Int): Boolean {
        return transaction {
            ReportTable.update({ (ReportTable.deviceId eq deviceId) and (ReportTable.userId eq userId) }) {
                it[frequency] = report.frequency
                it[mark] = report.mark
                it[screen] = report.screen
                it[body] = report.body
                it[width] = report.width
                it[height] = report.height
                it[density] = report.density
                it[ram] = report.ram
                it[totalSpace] = report.totalSpace
                it[gyroscope] = report.gyroscope
                it[versionOS] = report.versionOS
                it[batteryState] = report.batteryState
                it[level] = report.level
                it[dataStatus] = report.dataStatus
                it[gps] = report.gps
                it[bluetooth] = report.bluetooth
                it[audioReport] = report.audioReport
            } > 0
        }
    }

    /**
     * Получение последнего отчета пользователя
     * @param userId ID пользователя
     * @return объект Report или null, если отчетов нет
     */
    fun getLatestReport(userId: Int): Report? {
        return transaction {
            ReportTable.select { ReportTable.userId eq userId }
                .orderBy(ReportTable.id, SortOrder.DESC)
                .limit(1)
                .mapNotNull { rowToReport(it) }
                .singleOrNull()
        }
    }

    /**
     * Удаление отчета по ID и userId
     * @param id ID отчета
     * @param userId ID пользователя
     * @return true, если удаление было успешным, иначе false
     */
    fun deleteReportById(id: Int, userId: Int): Boolean {
        return transaction {
            ReportTable.deleteWhere { (ReportTable.id eq id) and (ReportTable.userId eq userId) } > 0
        }
    }

    /**
     * Преобразование строки результата из базы в объект Report
     */
    private fun rowToReport(row: ResultRow): Report {
        return Report(
            deviceId = row[ReportTable.deviceId],
            frequency = row[ReportTable.frequency],
            screen = row[ReportTable.screen],
            body = row[ReportTable.body],
            mark = row[ReportTable.mark],
            width = row[ReportTable.width],
            height = row[ReportTable.height],
            density = row[ReportTable.density],
            ram = row[ReportTable.ram],
            totalSpace = row[ReportTable.totalSpace],
            gyroscope = row[ReportTable.gyroscope],
            versionOS = row[ReportTable.versionOS],
            batteryState = row[ReportTable.batteryState],
            level = row[ReportTable.level],
            dataStatus = row[ReportTable.dataStatus],
            gps = row[ReportTable.gps],
            bluetooth = row[ReportTable.bluetooth],
            audioReport = row[ReportTable.audioReport]
        )
    }
}