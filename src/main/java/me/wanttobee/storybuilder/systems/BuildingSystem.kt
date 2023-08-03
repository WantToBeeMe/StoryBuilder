package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.gradients.Gradient
import me.wanttobee.storybuilder.gradients.SelectGradientMenu
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

object BuildingSystem {

    fun grid(player : Player, width: Int, height: Int){
        if(width <= 0 || height <= 0){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}grid size needs to be bigger then 0")
            return
        }
        val playerStory = StorySystem.getPlayersStory(player)
        val plane = playerStory.morphPlane ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}you need a plane for this action")
            return
        }

        SelectGradientMenu(player) { _ ->
            val br = playerStory.startBlockRecorder()
            val block = playerStory.currentGradient.get(0).createBlockData()
            val spread = 100
            for(x in 0..width){
                for(s in 0..spread){
                    val loc = plane.interpolate(x/width.toDouble(),s/spread.toDouble())
                    br.place(loc,block )
                }
            }
            for(y in 0..height){
                for(s in 0..spread){
                    val loc = plane.interpolate(s/spread.toDouble(),y/height.toDouble())
                    br.place(loc, block)
                }
            }
            br.finish()
        }.open(player)


    }

}