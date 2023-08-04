package me.wanttobee.storybuilder

import me.wanttobee.storybuilder.commands.ICommandSystem
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandEmptyLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandIntLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandPairLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch
import me.wanttobee.storybuilder.gradients.GradientListMenu
import me.wanttobee.storybuilder.systems.BuildingSystem
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import me.wanttobee.storybuilder.systems.playerStory.BlockRecorder
import org.bukkit.ChatColor

object SBCommands : ICommandSystem {
    override val helpText: String = "${ChatColor.GOLD}hold a feather to see the details of the plane you are working on"

    override val systemCommands : Array<ISystemCommand> = arrayOf(
            BlockRecorder.undo,
            BlockRecorder.redo,
            BuildingSystem.Grid,
            BuildingSystem.Fill,
            MakeGradient,
            GiveGradient,
            StorySystem.PosLeftTop,
            StorySystem.PosRightTop,
            StorySystem.PosLeftBottom,
            StorySystem.PosRightBottom,
            StorySystem.CurveFactor,
            StorySystem.Font,
            StorySystem.Samples,
    )

    object MakeGradient : ISystemCommand{
        override val exampleCommand: String = "/sd makeGradient"
        override val helpText: String = "able to make a new gradient to be used in other projects"
        override val baseTree: ICommandBranch = CommandEmptyLeaf("makeGradient"){p -> StorySystem.getPlayersStory(p).gradientMaker.open(p)}
    }

    object GiveGradient : ISystemCommand{
        override val exampleCommand: String = "/sd giveGradient"
        override val helpText: String = "gives the blocks of the gradient"
        override val baseTree: ICommandBranch = CommandEmptyLeaf("giveGradient"){p ->
            GradientListMenu {player, gradient ->
                gradient.giveToPlayer(player)
            }.open(p)
        }
    }

}