package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandPairLeaf<T,U>(arg : String, private val firstLeaf : ICommandLeaf<T>, private val secondLeaf : ICommandLeaf<U>, effect : (Player, Pair<T,U>) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Pair<T,U>>(arg,effect, emptyEffect) where T : Any, U : Any {
    constructor(arg: String, firstLeaf: ICommandLeaf<T>, secondLeaf: ICommandLeaf<U>) : this(arg,firstLeaf,secondLeaf,{_,_ -> })

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(tailArgs.size < argumentsNeeded) {
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else sender.sendMessage("${ChatColor.RED}not enough arguments found")
            return
        }
        val pair = validateValue(sender, tailArgs) ?: return
        effect.invoke(sender,pair)
    }

    override fun validateValue(sender: Player, tailArgs: Array<String>): Pair<T,U>? {
        if(tailArgs.isEmpty()) return null
        val firstValue : T = firstLeaf.validateValue(sender, tailArgs) ?: return null
        val secondValue : U = secondLeaf.validateValue(sender, tailArgs.copyOfRange(firstLeaf.argumentsNeeded, tailArgs.size)) ?: return null
        return Pair(firstValue,secondValue)
    }

    override fun nextTabComplete(sender: Player, fromArg: String, tailArgs: Array<String>): List<String> {
        //U or T tabComplete
        if(tailArgs.size +1 <= firstLeaf.argumentsNeeded)
            return firstLeaf.getTabComplete(sender,arrayOf(fromArg) + tailArgs)
        else
            return secondLeaf.getTabComplete(sender,tailArgs.copyOfRange(firstLeaf.argumentsNeeded-1, tailArgs.size))
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        //T tabComplete
        return firstLeaf.getTabComplete(sender, arrayOf(currentlyTyping))
    }
}