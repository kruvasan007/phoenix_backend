package phoenix.models

data class Rule(
    val conclusions: ArrayList<Conclusion>,
    val conditions: ArrayList<Condition>
)

class Conclusion : Statement() {
    private var weight: Double = 0.0

    fun getWeight(): Double {
        return weight
    }

    fun setWeight(weight: Double) {
        this.weight = weight
    }
}

class Condition : Statement() {

}
