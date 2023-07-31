package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandIntLeaf private constructor(arg : String,private val realTimeMin : (() -> Int)?,private val realTimeMax : (() -> Int)?, private val min:Int?,private val max:Int?,private val realTimePossibilities : (() -> Collection<Int>)?, private val possibilities : Collection<Int>?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Int>(arg,effect, emptyEffect) {
    constructor(arg : String,min:Int?, max:Int?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,null,null,min, max,null,null, effect, emptyEffect )
    constructor(arg : String, possibilities:Collection<Int>?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,null,null,null, null,null,possibilities, effect, emptyEffect )
    constructor(arg : String,min: (() -> Int)?, max: (() -> Int)?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,min,max,null, null,null,null, effect, emptyEffect )
    constructor(arg : String, possibilities:  (() -> Collection<Int>), effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,null,null,null, null,possibilities,null, effect, emptyEffect )

    override fun validateValue(sender: Player, tailArgs: Array<String>): Int? {
        if(tailArgs.isEmpty()) return null
        if(tailArgs.first() == ".."){
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else sender.sendMessage("${ChatColor.RED}these ${ChatColor.GRAY}..${ChatColor.RED} are there to convey that you could type any number ${ChatColor.DARK_RED}(Int)${ChatColor.RED}, but not literally ${ChatColor.GRAY}..")
            return null
        }
        var number = tailArgs.first().toIntOrNull() ?: run {
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid number ${ChatColor.DARK_RED}(Int)")
            return null
        }

        if(possibilities != null && !possibilities.contains(number)){
           sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid number ${ChatColor.DARK_RED}, you must chose from one of the suggested once")
           return null
        }
        if(realTimePossibilities != null && !realTimePossibilities.invoke().contains(number)){
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid number ${ChatColor.DARK_RED}, you must chose from one of the suggested once")
            return null
        }
        if(min != null && number < min && (possibilities == null && realTimePossibilities == null)){
            number = min
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}$min")
        }
        else if(max != null && number > max  && (possibilities == null && realTimePossibilities == null)){
            number = max
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}$max")
        }
        else if(realTimeMin != null && number < realTimeMin.invoke()  && (possibilities == null && realTimePossibilities == null)){
            number = realTimeMin.invoke()
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}${realTimeMin.invoke()}")
        }
        else if(realTimeMax != null && number > realTimeMax.invoke()  && (possibilities == null && realTimePossibilities == null)){
            number = realTimeMax.invoke()
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}has been clamped to ${ChatColor.GRAY}${realTimeMax.invoke()}")
        }

        return number
    }


    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if(possibilities != null){
            for(p in possibilities)
                if (p.toString().startsWith(currentlyTyping)) list.add(p.toString())
            return list
        }
        if(realTimePossibilities != null){
            for(p in realTimePossibilities.invoke())
                if (p.toString().startsWith(currentlyTyping)) list.add(p.toString())
            return list
        }
        if (min == null && max == null && realTimeMin == null && realTimeMax == null) {
            if ("" == currentlyTyping){
                list.add("..")
            }
            return list
        }
        val min = min ?: realTimeMin?.invoke()
        val max = max ?: realTimeMax?.invoke()
        if (min == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((max).toString())
                list.add((max!!- 1).toString())
                list.add((max  - 2).toString())
            }
        } else if (max == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((min).toString())
                list.add((min + 1).toString())
                list.add((min + 2).toString())
            }
        } else {
            if ("" == currentlyTyping) {
                if (max - min < 6) {
                    for (i in min  .. max)
                        list.add(i.toString())
                } else {
                    list.add("$min..$max")
                }
            }
        }
        return list
    }
}