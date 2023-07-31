package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

//we can assume that the tailArgs are always lowercase
class CommandTree(arg: String,private val branches : Array<ICommandBranch> ) : ICommandBranch(arg) {
    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(tailArgs.isEmpty()){
            sender.sendMessage("${ChatColor.RED} not enough arguments found")
            return
        }
        for(branch in branches) {
            if (branch.arg.lowercase() == tailArgs.first().lowercase()){
                branch.onCommand(sender, tailArgs.copyOfRange(1, tailArgs.size) )
                return
            }
        }
        sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()}${ChatColor.RED} is not a valid argument")
    }

    override fun nextTabComplete(sender: Player, fromArg:String, tailArgs: Array<String>): List<String> {
        for(branch in branches){
            if(branch.arg.lowercase() == fromArg.lowercase())
                return branch.getTabComplete(sender, tailArgs)
        }
        return emptyList()
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String) : List<String> {
        val list = mutableListOf<String>()
        for(branch in branches)
            if(branch.arg.lowercase().contains(currentlyTyping.lowercase())) list.add(branch.arg)
        return list
    }
}