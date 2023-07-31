package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandDoubleLeaf private constructor(arg : String,private val realTimeMin : (() -> Double)?,private val realTimeMax : (() -> Double)?, private val min:Double?, private val max:Double?,  effect : (Player, Double) -> Unit,emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Double>(arg,effect, emptyEffect) {
    constructor(arg : String, min:Double?, max:Double?,  effect : (Player, Double) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,null, null, min, max, effect, emptyEffect)
    constructor(arg : String, min:(() -> Double)?, max:(() -> Double)?,  effect : (Player, Double) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,min, max, null, null, effect, emptyEffect)

    override fun validateValue(sender: Player, tailArgs: Array<String>): Double? {
        if(tailArgs.isEmpty()) return null
        if(tailArgs.first() == ".."){
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else sender.sendMessage("${ChatColor.RED}these ${ChatColor.GRAY}..${ChatColor.RED} are there to convey that you could type any number ${ChatColor.DARK_RED}(Double)${ChatColor.RED}, but not literally ${ChatColor.GRAY}..")
            return null
        }
        var number = tailArgs.first().toDoubleOrNull() ?: run {
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid number ${ChatColor.DARK_RED}(Double)")
            return  null
        }

        if(min != null && number < min){
            number = min
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}$min")
        }
        else if(max != null && number > max){
            number = max
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}$max")
        }
        else if(realTimeMin != null && number < realTimeMin.invoke()){
            number = realTimeMin.invoke()
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}${realTimeMin.invoke()}")
        }
        else if(realTimeMax != null && number > realTimeMax.invoke()){
            number = realTimeMax.invoke()
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}${realTimeMax.invoke()}")
        }
        return number
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if (min == null && max == null && realTimeMin == null && realTimeMax == null) {
            if ("" == currentlyTyping)
                list.add("..")
            return list
        }
        val min = min ?: realTimeMin?.invoke()
        val max = max ?: realTimeMax?.invoke()
        if (min == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((max).toString())
                list.add((max!!- 1.0).toString())
                list.add((max  - 2.0).toString())
            }
        } else if (max == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((min).toString())
                list.add((min + 1.0).toString())
                list.add((min + 2.0).toString())
            }
        } else {
            if ("" == currentlyTyping) {
                val distance = max - min
                if (distance < 4) {
                    for (i in 0  .. distance.toInt())
                        list.add((min + i.toDouble()).toString())
                } else {
                    list.add((min).toString())
                    list.add((min + 1.0).toString())
                    list.add("..")
                    list.add((max).toString())
                    list.add((max - 1.0).toString())
                }
            }
        }
        return list
    }
}