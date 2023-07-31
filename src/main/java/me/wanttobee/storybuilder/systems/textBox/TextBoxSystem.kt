package me.wanttobee.storybuilder.systems.textBox

import me.wanttobee.storybuilder.SBPlugin
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

object TextBoxSystem : Listener {
    private val sounds = false
    private val textBoxes : MutableMap<Player,TextBox> = mutableMapOf()

    fun everyTick(){
        for((_, box) in textBoxes){
            box.everyTick()
        }
    }


    @EventHandler
    fun onFeatherInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            val item = event.item ?: return
            if (item.type == Material.FEATHER) {
                val textBox = textBoxes.get(player)
                if(textBox == null){
                    textBoxes[player] = TextBox(player)
                    player.sendMessage("${ChatColor.GREEN}created a textBox")
                    player.sendMessage("${ChatColor.GRAY}(Drop the feather if you want to remove it)")
                    player.sendMessage("${ChatColor.GRAY}(Right click to adjust)")
                    event.isCancelled = true
                    return
                }
                if(textBox.isEditing()){
                    textBox.editCorner(null)
                    if(sounds){
                        player.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 1f, 1f)
                        player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, SoundCategory.MASTER, 0.3f, 1f)
                    }
                    event.isCancelled = true
                    return
                }
                val distanceCheck = 30
                val distanceThreshold = 1.0 // Adjust this value as needed
                val eyeLocation = player.eyeLocation
                val direction = eyeLocation.direction

                for(i in 1 .. distanceCheck){
                    eyeLocation.add(direction)
                    var clicked : Location? = null
                    if(textBox.leftTop.distance(eyeLocation) <= distanceThreshold){
                        clicked = textBox.leftTop
                    } else if(textBox.rightTop.distance(eyeLocation) <= distanceThreshold){
                        clicked = textBox.rightTop
                    } else if(textBox.leftBot.distance(eyeLocation) <= distanceThreshold){
                        clicked = textBox.leftBot
                    }else if(textBox.rightBot.distance(eyeLocation) <= distanceThreshold){
                        clicked = textBox.rightBot
                    }
                    if(clicked != null){
                        textBox.editCorner(clicked)
                        if(sounds){
                            player.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 1f, 1f)
                            player.playSound(player.location, Sound.BLOCK_CANDLE_PLACE, SoundCategory.MASTER, 0.7f, 1f)
                        }
                        event.isCancelled = true
                        return
                    }
                }
                if(sounds)
                    player.playSound(player.location, Sound.BLOCK_CANDLE_EXTINGUISH, SoundCategory.MASTER, 0.7f, 1f)
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun playerLeaveEvent(event: PlayerQuitEvent) {
        val player = event.player
        textBoxes.remove(player)
    }
    @EventHandler
    fun onHotBarDropItem(event: PlayerDropItemEvent) {
        val player = event.player

        val item = event.itemDrop.itemStack
        if (item.type == Material.FEATHER && textBoxes.containsKey(player)) {
            textBoxes.remove(player)
            player.sendMessage("${ChatColor.GREEN}textBox has been removed")
            player.playSound(player.location, Sound.BLOCK_CANDLE_EXTINGUISH, SoundCategory.MASTER, 0.7f, 1f)
            event.isCancelled = true
        }
    }

}