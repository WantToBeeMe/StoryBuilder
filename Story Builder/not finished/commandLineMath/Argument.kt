package me.wanttobee.maseg.commandLineMath

class Argument private constructor (private val value : Double?,private val operator : Triple<((Double, Double) -> Double),String,Int>?,private val equals : String? ) {
    constructor(value : Double) : this(value, null, null)
    constructor(operator : ((Double, Double) -> Double), name: String, priority : Int) : this(null, Triple(operator,name,priority), null)
    constructor(equals :String) : this(null, null, equals)

    fun isValue() : Boolean{ return value != null }
    fun isOperator() : Boolean{ return operator != null }
    fun isEquals() : Boolean{ return equals != null }

    fun getValue() : Double?{ return value }
    fun getOperator() : Triple<((Double, Double) -> Double),String,Int>? {return operator}
    fun getEquals() : String? {return equals}

    override fun toString(): String {
        if(value != null) return value.toString()
        if(operator != null) return operator.second
        return equals ?: "null"
    }
}