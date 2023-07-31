package me.wanttobee.maseg.commandLineMath

import me.wanttobee.maseg.MASEGPlugin

//cmd, but minecraft, so mcd
class MathTable {
    val plugin = MASEGPlugin.instance
    var lastEquationResult : Int? = null

    fun finishLine(args : Array<String>) : Int?{

        return null
    }

    fun splitInEquations(args : Array<String>) : Array<Array<String>>{
        val finishedEquations : MutableList<Array<String>> = mutableListOf()
        val currentEquation : MutableList<String> = mutableListOf()
        for(arg in args){
            currentEquation.add(arg)
            if(arg.startsWith("=")){
                finishedEquations.add(currentEquation.toTypedArray())
                currentEquation.clear()
            }
        }
        return finishedEquations.toTypedArray()
    }

    fun calculateEquation(args : Array<String>) : Pair<Double, String?>?{
        val arguments = evaluateEquation(args) ?: return null

        var line = ""
        for(arg in arguments)
            line += arg
        plugin.logger.info(line)

        return null
        val lastEquals = arguments.last().isEquals()
        val indices= if(lastEquals) arguments.indices-1 else arguments.indices
        var shouldBeValue = true
        for(i in indices){
            if(shouldBeValue != arguments[i].isValue()) return null
            if(shouldBeValue != arguments[i].isOperator()) return null
            shouldBeValue = !shouldBeValue
        }
        if(shouldBeValue) return null


    }

    private fun evaluateEquation(args : Array<String>) :  Array<Argument>?{
        val arguments = Array(args.size) {_ -> Argument(0.0) }
        for(i in args.indices)
            arguments[i] = evaluateArgument(args[i]) ?: return null
        return arguments
    }


    private fun evaluateArgument(arg : String) : Argument? {
        if(arg.toDoubleOrNull() != null) return Argument(arg.toDouble())
        if (arg.contains("=")) return Argument(arg)
        if (arg.length == 1 && "+-*/%".contains(arg)) {
            val operatorFunction: (Double, Double) -> Double = when (arg) {
                "+" -> { a, b -> a + b }
                "-" -> { a, b -> a - b }
                "*" -> { a, b -> a * b }
                "/" -> { a, b -> a / b }
                "%" -> { a, b -> a % b }
                else -> return null
            }
            return Argument(operatorFunction, arg, 1)
        }
        val operatorRegex = Regex("[+*/\\-%]")
        val operatorMatch = operatorRegex.find(arg) ?: return null
        val operator = operatorMatch.value
        val operands = arg.split(operator)
        val firstOperand = operands[0].toDoubleOrNull() ?: return null
        val secondOperand = operands[1].toDoubleOrNull() ?: return null
        val result = when (operator) {
            "+" -> firstOperand + secondOperand
            "-" -> firstOperand - secondOperand
            "*" -> firstOperand * secondOperand
            "/" -> firstOperand / secondOperand
            "%" -> firstOperand % secondOperand
            else -> return null
        }
        return Argument(result)
    }

}