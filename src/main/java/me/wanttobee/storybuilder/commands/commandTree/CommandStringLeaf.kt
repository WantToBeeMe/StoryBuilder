package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandStringLeaf private constructor(arg : String,private val realTimePossibilities : (() -> Array<String>)?, private val possibilities : Array<String>?, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<String>(arg,effect, emptyEffect) {
    constructor (arg : String,possibilities : Array<String>?, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg, null, possibilities, effect, emptyEffect)
    constructor (arg : String, realTimePossibilities : () -> Array<String>, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null) : this(arg, realTimePossibilities,null, effect, emptyEffect)
    override fun validateValue(sender: Player, tailArgs: Array<String>): String? {
        if(tailArgs.isEmpty()) return null
        if(possibilities == null && realTimePossibilities == null )
            return tailArgs.first()
        if(possibilities != null){
            for(pos in possibilities){
                if(pos.lowercase() == tailArgs.first().lowercase())
                    return tailArgs.first()
            }
        }
        if(realTimePossibilities != null){
            for(pos in realTimePossibilities.invoke()){
                if(pos.lowercase() == tailArgs.first().lowercase())
                    return tailArgs.first()
            }
        }
        sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid argument")
        return null
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if(possibilities == null && realTimePossibilities == null){
            if("" == currentlyTyping)
                list.add("...")
        } else if(possibilities != null) {
            for (pos in possibilities) {
                if (pos.lowercase().contains(currentlyTyping.lowercase()))
                    list.add(pos)
            }
        } else{
            for (pos in realTimePossibilities!!.invoke()) {
                if (pos.lowercase().contains(currentlyTyping.lowercase()))
                    list.add(pos)
            }
        }
        return list
    }
}