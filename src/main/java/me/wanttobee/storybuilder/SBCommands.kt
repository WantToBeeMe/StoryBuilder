package me.wanttobee.storybuilder

import me.wanttobee.storybuilder.commands.ICommandSystem
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandEmptyLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandIntLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandPairLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch
import me.wanttobee.storybuilder.gradients.GradientListMenu
import me.wanttobee.storybuilder.gradients.SelectGradientMenu
import me.wanttobee.storybuilder.systems.BuildingSystem
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import me.wanttobee.storybuilder.systems.TextCommands
import me.wanttobee.storybuilder.systems.playerStory.BlockRecorder
import org.bukkit.ChatColor
import org.bukkit.Material

object SBCommands : ICommandSystem {
    override val helpText: String = "${ChatColor.GOLD}hold a feather to see the details of the plane you are working on"

    override val systemCommands : Array<ISystemCommand> = arrayOf(
            TextCommands,
            BlockRecorder.undo,
            BlockRecorder.redo,
            Grid,
            MakeGradient,
            GiveGradient,
            StorySystem.posLeftTop,
            StorySystem.posRightTop,
            StorySystem.posLeftBottom,
            StorySystem.posRightBottom,
            StorySystem.curveFactor,
    )

    object Grid : ISystemCommand{
        override val exampleCommand: String = "/sd grid [Int/width] [Int/height]"
        override val helpText: String = "to generate a grid pattern on the plane"
        override val baseTree: ICommandBranch = CommandPairLeaf( "grid",
                CommandIntLeaf("width", 1, null, {_,_ -> }),
                CommandIntLeaf("height", 1, null, {_,_ -> }),
                {player, pair ->
                    BuildingSystem.grid(player, pair.first, pair.second)
                })
    }

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