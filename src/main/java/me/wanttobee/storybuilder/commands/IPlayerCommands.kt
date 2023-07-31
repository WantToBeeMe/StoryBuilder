package me.wanttobee.storybuilder.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

//the base interface of the different base command groups
//this provides some simple base structure like a default Help method when nothing is put in, and a checker that it is defenitly a player
interface IPlayerCommands : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("only a player can run this command")
            return true
        }
        if (args.isEmpty()) {
            help(sender)
            return true
        }

        if(args[0].lowercase() == "help") {
            help(sender)
            return true
        }
        return onCommand(sender, Array(args.size) { index -> args[index] } )
    }
    fun onCommand(sender: Player, args : Array<String>) : Boolean

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        val list : MutableList<String> = mutableListOf();

        if (sender !is Player || args.isEmpty())
            return list
        val args = Array(args.size) { index -> args[index] }

        if(args.size == 1 && "help".startsWith(args[0].lowercase()))
            return onTabComplete(sender, args) + "help"

        return onTabComplete(sender, args)
    }
    fun onTabComplete(sender: Player, args : Array<String>) : List<String>

    fun help(sender: Player)
}