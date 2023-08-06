package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.*
import me.wanttobee.storybuilder.gradients.SelectGradientMenu
import me.wanttobee.storybuilder.gradients.SelectGradientMenu2
import me.wanttobee.storybuilder.morphPlane.MorphPlane
import me.wanttobee.storybuilder.systems.playerStory.PlayersStory
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object BuildingSystem {

    fun grid(player : Player, width: Int, height: Int, skip : Boolean){
        if(width <= 0 || height <= 0){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}grid size needs to be bigger then 0")
            return
        }
        val playerStory = StorySystem.getPlayersStory(player)
        val plane = playerStory.morphPlane ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}you need a plane for this action")
            return
        }
        if(!plane.isComplete()){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}the plane isn't complete")
            return
        }
        if(skip) runGridBuilder(playerStory, plane, width, height)
        else{
            SelectGradientMenu2(player) { _ ->
                runGridBuilder(playerStory, plane, width, height)
            }.open(player)
        }

    }
    private fun runGridBuilder(playerStory : PlayersStory, plane : MorphPlane, width: Int, height: Int){
        playerStory.runBlockRecorderAsync { br ->
            val block = playerStory.currentGradient.get(0).createBlockData()
            val spread = playerStory.samples
            for(x in 0..width){
                for(s in 0..spread){
                    val loc = plane.interpolate(x/width.toDouble(),s/spread.toDouble())!!
                    br.place(loc,block )
                }
            }
            for(y in 0..height){
                for(s in 0..spread){
                    val loc = plane.interpolate(s/spread.toDouble(),y/height.toDouble())!!
                    br.place(loc, block)
                }
            }
        }
    }

    fun fill(player : Player, skip : Boolean){
        val playerStory = StorySystem.getPlayersStory(player)
        val plane = playerStory.morphPlane ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}you need a plane for this action")
            return
        }
        if(!plane.isComplete()){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}the plane isn't complete")
            return
        }

        if(skip) runFillBuilder(playerStory, plane)
        else{
            SelectGradientMenu(player) { _ ->
                runFillBuilder(playerStory, plane)
            }.open(player)
        }
    }
    private fun runFillBuilder(playerStory: PlayersStory,plane : MorphPlane){
        playerStory.runBlockRecorderAsync { br ->
            val block = playerStory.currentGradient.get(0).createBlockData()
            val spread = playerStory.samples
            for(x in 0..spread){
                for(y in 0..spread){
                    val loc = plane.interpolate(x/spread.toDouble(),y/spread.toDouble())!!
                    br.place(loc,block)
                }
            }
        }
    }

    object Grid : ISystemCommand {
        override val exampleCommand: String = "/sd grid [Int/width] [Int/height]"
        override val helpText: String = "to generate a grid pattern on the plane"
        override val baseTree: ICommandBranch = CommandPairLeaf( "grid",
            CommandIntLeaf("width", 1, null, {_,_ -> }),
            CommandIntLeaf("height", 1, null, {_,_ -> }),
            {player, pair ->
                grid(player, pair.first, pair.second, false)
            })
    }

    object Fill : ISystemCommand{
        override val exampleCommand: String = "/sd fill"
        override val helpText: String = "to generate a grid pattern on the plane"
        override val baseTree: ICommandBranch = CommandStringLeaf("fill", null, {player,value -> fill(player, true) },
        {player -> fill(player, false) })
    }

}