package me.wanttobee.storybuilder.commands

import me.wanttobee.storybuilder.SBPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player

//the base structure of the composite design
//the flowerPot of the branches and leaves if you will
//this is the basis of that system and thus requires the different branches
interface ICommandSystem : IPlayerCommands {
    val helpText : String
    val systemCommands : Array<ISystemCommand>

    override fun onCommand(sender: Player, args: Array<String>): Boolean {
        for(sysCom in systemCommands){
            if (sysCom.baseTree.arg.lowercase() == args.first().lowercase()) {
                sysCom.baseTree.onCommand(sender, args.copyOfRange(1, args.size))
                break
            }
        }
        return true
    }

    override fun help(sender: Player){
        sender.sendMessage("${SBPlugin.title} $helpText")
        val helperTab : (String)-> String = { h -> "${ChatColor.YELLOW}$h${ChatColor.WHITE}"}
        for(sysCom in systemCommands){
            sender.sendMessage("- ${helperTab(sysCom.baseTree.arg + ":")} ${sysCom.helpText} ${ChatColor.GRAY}${sysCom.exampleCommand}")
        }
    }

    override fun onTabComplete(sender: Player, args: Array<String>): List<String> {
        val list : MutableList<String> = mutableListOf();

        if(args.size == 1){
            for(sysCom in systemCommands){
                if(sysCom.baseTree.arg.lowercase().startsWith(args.first().lowercase()))
                    list.add(sysCom.baseTree.arg)
            }
            return list
        }

        for(sysCom in systemCommands){
            if(sysCom.baseTree.arg.lowercase() == args.first().lowercase())
                return sysCom.baseTree.getTabComplete(sender, args.copyOfRange(1, args.size) )
        }
        return list
    }

}