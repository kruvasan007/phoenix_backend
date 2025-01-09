import phoenix.models.ActivatedFuzzySet
import phoenix.models.IFuzzySet
import phoenix.models.Rule
import phoenix.models.UnionOfFuzzySets

class Algorithm {
    private var conclusionsCount = 0
    private var conditionsCount = 0
    private var outputVariablesCount = 0
    private var inputVariablesCount = 0
    private var rulesCount = 0
    private var rules: ArrayList<Rule>? = null


    // PUBLIC

    fun run(db: List<Double>, rules: ArrayList<Rule>): List<Double> {
        return arrayListOf()
    }

    // PRIVATE

    /** Сбор данных **/
    private fun accumulation(activatedFuzzySet: ArrayList<ActivatedFuzzySet>)
            : List<UnionOfFuzzySets> {
        var unionsOfFuzzySets = arrayListOf<UnionOfFuzzySets>();

        for (rule in rules!!) {
            for (conclusion in rule.conclusions) {
                val id = conclusion.getVariable().id
                unionsOfFuzzySets[id].addFuzzySet(activatedFuzzySet[id]);
            }

        }
        return unionsOfFuzzySets

    }

    private fun defuzzification(unionOfFuzzySets: ArrayList<UnionOfFuzzySets>)
            : ArrayList<Double> {
        val defuzzificationRes = arrayListOf<Double>()
        for (i in 0..outputVariablesCount) {
            val i1: Double = Math.i(unionOfFuzzySets.get(i), true)
            val i2: Double = integral(unionOfFuzzySets.get(i), false)
            defuzzificationRes[i] = i1 / i2
        }
        return defuzzificationRes
    }

    private fun activation(minimizationSet: ArrayList<Double>)
            : ArrayList<ActivatedFuzzySet> {
        var i = 0;
        var activatedFuzzySets = arrayListOf<ActivatedFuzzySet>();
        var activated = arrayListOf<Double>();
        for (rule in rules!!) {
            for (conclusion in rule.conclusions) {
                activated.add(minimizationSet[i] * conclusion.getWeight())
                val activatedFuzzySet = conclusion.getTerm() as ActivatedFuzzySet? ?: return activatedFuzzySets
                activatedFuzzySet.setTruthDegree(activated[i]);
                activatedFuzzySets.add(activatedFuzzySet);
                i++;
            }
        }
        return activatedFuzzySets
    }

    private fun aggregation(muArray: List<Double>)
            : ArrayList<Double> {
        var i = 0;
        var j = 0;
        var outputMin = arrayListOf<Double>();
        for (rule in rules!!) {
            var truthOfConditions = 1.0;
            for (condition in rule.conditions) {
                truthOfConditions = minOf(truthOfConditions, muArray[i]);
                i++;
            }
            outputMin.add(truthOfConditions);
            j++;
        }
        return outputMin;
    }

    private fun fuzzification(values: ArrayList<Double>)
            : ArrayList<Double> {
        var i = 0;
        val outputMu = arrayListOf<Double>()
        for (rule in rules!!) {
            for (condition in rule.conditions) {
                var j = condition.getVariable().id
                var term = condition.getTerm()
                outputMu.add(term.getValue(values[i]))
                i++
            }
        }
        return outputMu
    }

    private fun integral(fuzzySet: IFuzzySet, flag: Boolean)
            : ArrayList<Double> {
        return arrayListOf()
    }

}