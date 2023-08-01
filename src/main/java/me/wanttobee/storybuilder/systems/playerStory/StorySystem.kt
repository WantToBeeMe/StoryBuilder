package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandEmptyLeaf
import me.wanttobee.storybuilder.commands.commandTree.CommandIntLeaf
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object StorySystem  : Listener {

    val stories : MutableMap<Player, PlayersStory> = mutableMapOf()
    fun getPlayersStory(player :Player) : PlayersStory {
        if(!stories.containsKey(player))
            stories[player] = PlayersStory(player)
        return stories[player]!!
    }

    fun everyTick(){
        for((_,story) in stories)
            story.tick()
    }

    fun loadFont(player : Player, path : String?){
        if(path != null) getPlayersStory(player).loadFont(path)
        else getPlayersStory(player).currentFontMessage()
    }

    @EventHandler
    fun playerLeaveEvent(event: PlayerQuitEvent) {
        val player = event.player
        val story = stories.remove(player) ?: return
        story.clear()
    }
    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player
        stories[player] = PlayersStory(player)
    }

    @EventHandler
    fun onHotBarDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val item = event.itemDrop.itemStack
        if (item.type == Material.FEATHER)
            getPlayersStory(player).onFeatherDrop(event)
    }
    @EventHandler
    fun onFeatherInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        if (item.type == Material.FEATHER )
            getPlayersStory(player).onFeatherInteract(event)
    }





    object posLeftTop : ISystemCommand {
        override val exampleCommand: String= "/sd poslt"
        override val helpText: String = "set the left top position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("poslt") { p ->
            val plane = getPlayersStory(p).getPlane()
            plane.setLeftTop(p.location)
        }
    }

    object posRightTop : ISystemCommand {
        override val exampleCommand: String= "/sd posrt"
        override val helpText: String = "set the right top position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("posrt") { p ->
            val plane = getPlayersStory(p).getPlane()
            plane.setRightTop(p.location)
        }
    }

    object posLeftBottom : ISystemCommand {
        override val exampleCommand: String= "/sd poslb"
        override val helpText: String = "set the left bottom position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("poslb") { p ->
            val plane = getPlayersStory(p).getPlane()
            plane.setLeftBottom(p.location)
        }
    }

    object posRightBottom : ISystemCommand {
        override val exampleCommand: String= "/sd posrb"
        override val helpText: String = "set the right bottom position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("posrb") { p ->
            val plane = getPlayersStory(p).getPlane()
            plane.setRightBottom(p.location)
        }
    }
}