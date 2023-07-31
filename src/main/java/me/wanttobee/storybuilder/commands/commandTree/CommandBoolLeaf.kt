package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandBoolLeaf(arg : String, effect : (Player, Boolean) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Boolean>(arg,effect, emptyEffect) {

    override fun validateValue(sender: Player, tailArgs: Array<String>): Boolean? {
        if(tailArgs.isEmpty()) return null
        val bool = tailArgs.first().toBooleanStrictOrNull() ?: run {
            sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not a valid boolean ${ChatColor.DARK_RED}(true/false)")
            return null
        }
        return bool
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if("true".startsWith(currentlyTyping.lowercase())) list.add("true")
        if("false".startsWith(currentlyTyping.lowercase())) list.add("false")
        return list
    }
}