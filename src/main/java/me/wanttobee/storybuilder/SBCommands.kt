package me.wanttobee.storybuilder

import me.wanttobee.storybuilder.commands.ICommandSystem
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandEmptyLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch
import me.wanttobee.storybuilder.gradients.GradientListMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.buildingSystem.BuildingSystem
import me.wanttobee.storybuilder.buildingSystem.font.TextBuildingSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import me.wanttobee.storybuilder.playerStory.BlockRecorder
import org.bukkit.ChatColor

object SBCommands : ICommandSystem {
    override val helpText: String = "${ChatColor.GOLD}hold a feather to see the details of the plane you are working on"

    override val systemCommands : Array<ISystemCommand> = arrayOf(
            BlockRecorder.undo,
            BlockRecorder.redo,
            BuildingSystem.Grid,
            BuildingSystem.Fill,
            TextBuildingSystem.Text,
            MakeGradient,
            GiveGradient,
            StorySystem.OpenPlaneEditor,
            StorySystem.CurveFactor,
            StorySystem.FontCommand,
            StorySystem.Samples,
            StorySystem.FontSize,
            InventoryMenuSystem.MenuDebug,
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