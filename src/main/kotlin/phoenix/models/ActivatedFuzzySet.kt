package phoenix.models

class ActivatedFuzzySet : FuzzySet() {
    private var truthDegree: Double = 0.0

    // PUBLIC

    fun setTruthDegree(value: Double) {
        this.truthDegree = value
    }

    override fun getValue(value: Double): Double {
        TODO("Not yet implemented")
    }

    // PRIVATE

    private fun getActivatedValue(db: Double): Double {
        return Math.min(getValue(db), truthDegree)
    }
}