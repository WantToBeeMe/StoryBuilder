package me.wanttobee.maseg.commandLineMath

import me.wanttobee.maseg.commands.IPlayerCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object CommandLineMathCommands : IPlayerCommands {
    //evaluators :nerd:
    private val evaluators : MutableMap<Player, MathTable> = mutableMapOf()

    override fun onCommand(sender: Player, args: Array<String>): Boolean {
        if(!evaluators.containsKey(sender)) evaluators[sender] = MathTable()
        val table : MathTable = evaluators[sender]!!
        val result = table.finishLine(args)
        if(result == null) sender.sendMessage(" §cThis equation is invalid")
        else sender.sendMessage("§6 $result")
        return true
    }

    override fun onTabComplete(sender: Player, args: Array<String>): List<String> {
        if(!evaluators.containsKey(sender)) evaluators[sender] = MathTable()
        val table : MathTable = evaluators[sender]!!
        table.calculateEquation(args)
        if(args.size > 1)
            if(args[args.size-2].startsWith("="))
                return listOf("54")
        return emptyList()
    }

    override fun help(sender: Player) {
        val title = "${ChatColor.GRAY}[${ChatColor.GOLD}MAthSEG${ChatColor.GRAY}]${ChatColor.RESET}"
        if(!evaluators.containsKey(sender)) evaluators[sender] = MathTable()
        val table : MathTable = evaluators[sender]!!

    }


}