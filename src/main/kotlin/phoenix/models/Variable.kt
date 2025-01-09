package phoenix.models

data class Variable (
    // PUBLIC
    var terms: Set<IFuzzySet>,
    // PRIVATE
    val id : Int = 0
)
