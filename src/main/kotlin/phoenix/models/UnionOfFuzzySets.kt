package phoenix.models

import kotlin.math.max

class UnionOfFuzzySets {
    private lateinit var fuzzySets: ArrayList<IFuzzySet>

    // PUBLIC

    fun getValue(db: Double): Double {
        return 0.0
    }

    fun addFuzzySet(fuzzySet: IFuzzySet) {
        this.fuzzySets.add(fuzzySet)
    }

    // PRIVATE

    private fun getMaxValue(db: Double): Double {
        var result = 0.0

        for (fuzzySet in fuzzySets) {
            result = max(result, fuzzySet.getValue(db))
        }

        return result
    }

}