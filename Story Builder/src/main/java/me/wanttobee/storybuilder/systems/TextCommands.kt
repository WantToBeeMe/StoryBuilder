package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandStringLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandVarargLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch

object TextCommands : ISystemCommand {
    override val exampleCommand: String ="/sb text [font] [vararg/strings]"
    override val helpText: String = "place text in the world"
    override val baseTree: ICommandBranch = CommandVarargLeaf("text",
        CommandStringLeaf("words" ,null, {_,_->} ),
        false,
        {commander, text ->
            for(t in text)
                commander.sendMessage(t)
        }
    )
}