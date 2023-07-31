package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

abstract class ICommandLeaf<T>(arg : String, protected val effect : (Player, T) -> Unit, protected val emptyEffect : ((Player) -> Unit)? = null) : ICommandBranch(arg) {

    open val argumentsNeeded = 1

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(tailArgs.isEmpty()) {
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else sender.sendMessage("${ChatColor.RED}no argument found")
            return
        }
        val value = validateValue(sender, tailArgs) ?: return
        effect.invoke(sender,value)
    }

    abstract fun validateValue(sender : Player, tailArgs: Array<String>) : T?
}