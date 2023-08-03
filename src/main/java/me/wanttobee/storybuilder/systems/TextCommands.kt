package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.*
import me.wanttobee.storybuilder.systems.playerStory.StorySystem


object TextCommands : ISystemCommand {
    override val exampleCommand: String ="/sb text [font] [vararg/strings]"
    override val helpText: String = "place text in the world"


    private val fontTree = CommandStringLeaf("font", { FontFileSystem.getAllFiles(false) },
        { p, fileName -> StorySystem.loadFont(p,fileName) },
        { p -> StorySystem.loadFont(p,null) })

    override val baseTree: ICommandBranch = CommandTree("text", arrayOf(
        fontTree,
    ))
}