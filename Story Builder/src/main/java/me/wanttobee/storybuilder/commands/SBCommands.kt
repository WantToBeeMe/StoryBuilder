package me.wanttobee.storybuilder.commands

import me.wanttobee.storybuilder.systems.TextCommands
import org.bukkit.ChatColor

object SBCommands : ICommandSystem {
    override val helpText: String = "${ChatColor.GRAY}/mg${ChatColor.WHITE} for Maseg Games"

    override val systemCommands : Array<ISystemCommand> = arrayOf(
        TextCommands
    )
}