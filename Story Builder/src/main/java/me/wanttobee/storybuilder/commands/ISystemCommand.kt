package me.wanttobee.storybuilder.commands

import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch

//an interface to build your command tree from
interface ISystemCommand {

    val exampleCommand : String
    val helpText : String

    val baseTree : ICommandBranch
}



