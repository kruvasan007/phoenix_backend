package phoenix.models

open class Statement {
    private lateinit var term : FuzzySet
    private lateinit var variable : Variable

    fun getTerm() : FuzzySet {
        return term
    }

    fun setTerm(term: FuzzySet) {
        this.term = term
    }

    fun getVariable() : Variable {
        return variable
    }

    fun setVariable(variable: Variable) {
        this.variable = variable
    }
}
