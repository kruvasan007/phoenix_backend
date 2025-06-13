package phoenix.models

data class ReportAnswer (
    var mark : String,
    var model : String,
    var condition : String,
    var price : Double?,
    var url : String,
    var report_id : Int,
)