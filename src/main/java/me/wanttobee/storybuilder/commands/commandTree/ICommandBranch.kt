package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.entity.Player

//the of the whole composite command structure (branches and leaves)
abstract class ICommandBranch(val arg : String ) {

    abstract fun onCommand(sender : Player, tailArgs : Array<String>)


    //to get the tabComplete
    fun getTabComplete(sender : Player, tailArgs : Array<String>) : List<String>{
        if(tailArgs.size > 1) return nextTabComplete(sender,tailArgs.first(), tailArgs.copyOfRange(1, tailArgs.size))
        else if(tailArgs.size == 1) return thisTabComplete(sender, tailArgs.first())
        return emptyList()
    }
    // if it's not here, then this will send the next tabComplete
    // if it's a leaf, that means you can leave it as it is, there is no next tabComplete
    protected open fun nextTabComplete(sender : Player, fromArg: String, tailArgs : Array<String>) : List<String>{
        //the tailArgs are already cut short for the next tabComplete, you don't have to do that anymore
        return emptyList()
    }

    //what happens here in this branch
    protected abstract fun thisTabComplete(sender : Player, currentlyTyping: String): List<String>
}