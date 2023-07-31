package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandStringLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandTree
import me.wanttobee.storybuilder.commands.commandTree.CommandVarargLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch


object TextCommands : ISystemCommand {
    override val exampleCommand: String ="/sb text [font] [vararg/strings]"
    override val helpText: String = "place text in the world"


    private val fontTree = CommandStringLeaf("font", { FontSystem.getAllFiles(false) },
        { p, fileName -> FontSystem.loadFont(p, fileName) },
        { p -> FontSystem.currentFontMessage(p) })

    private val buildTree = CommandVarargLeaf("build",
        CommandStringLeaf("words" ,null, {_,_->} ),
        false,
        {commander, text ->
            BuildingSystem.buildSentence(commander,text)
        }
    )
    override val baseTree: ICommandBranch = CommandTree("text", arrayOf(
        buildTree,
        fontTree
    ))
}