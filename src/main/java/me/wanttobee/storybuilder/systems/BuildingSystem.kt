package me.wanttobee.storybuilder.systems

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

object BuildingSystem {

    fun grid(player : Player, width: Int, height: Int, blockData : BlockData){
        if(width <= 0 || height <= 0){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}grid size needs to be bigger then 0")
            return
        }
        val playerStory = StorySystem.getPlayersStory(player)
        val plane = playerStory.morphPlane ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}you need a plane for this action")
            return
        }
        val br = playerStory.blockRecorder.start()
        val spread = 100
        for(x in 0..width){
            for(s in 0..spread){
                val loc = plane.interpolate(x/width.toDouble(),s/spread.toDouble())
                br.place(loc, blockData)
            }
        }
        for(y in 0..height){
            for(s in 0..spread){
                val loc = plane.interpolate(s/spread.toDouble(),y/height.toDouble())
                br.place(loc, blockData)
            }
        }
        br.finish()
    }

}