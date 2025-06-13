package phoenix.models

import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("body")
    var body: String,
    @SerializedName("screen")
    var screen: String,
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
    var gps: Boolean,
    @SerializedName("bluetooth")
    var bluetooth: Boolean,
    @SerializedName("audioReport")
    val audioReport: Boolean
)