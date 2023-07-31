package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.entity.Player

class CommandVarargLeaf<T>(arg : String, private val argLeaf : ICommandLeaf<T>, private val canReturnEmpty : Boolean, effect : (Player, List<T>) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<List<T>>(arg, effect, emptyEffect) {

    override val argumentsNeeded = 1000

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(canReturnEmpty && tailArgs.isEmpty())
            effect.invoke(sender, emptyList())
        else super.onCommand(sender, tailArgs)
    }
    override fun validateValue(sender : Player, tailArgs: Array<String>) : List<T>? {
        val listT : MutableList<T> =  mutableListOf()
        for(arg in tailArgs){
            val potentialT = argLeaf.validateValue(sender, arrayOf(arg)) ?: return null
            listT.add(potentialT)
        }
        return listT
    }

    override fun nextTabComplete(sender: Player, fromArg: String, tailArgs: Array<String>): List<String> {
        return argLeaf.getTabComplete(sender, arrayOf(tailArgs.last()))
    }
    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        return argLeaf.getTabComplete(sender, arrayOf(currentlyTyping))
    }
}