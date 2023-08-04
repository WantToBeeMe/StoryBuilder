package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.*
import me.wanttobee.storybuilder.systems.FontFileSystem
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

    fun loadFont(player : Player, path : String?) : String?{
        return if(path != null) getPlayersStory(player).loadFont(path)
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





    object PosLeftTop : ISystemCommand {
        override val exampleCommand: String= "/sd poslt"
        override val helpText: String = "set the ${ChatColor.GREEN}left top${ChatColor.RESET} position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("poslt") { p ->
            val plane = getPlayersStory(p).getPlane()
            val location = p.location
            plane.setLeftTop(location.blockLocation())
            p.sendMessage("${SBPlugin.title}set ${ChatColor.GREEN}left top${ChatColor.RESET} to your current location ${ChatColor.GRAY}(${location.blockX}/${location.blockY}/${location.blockZ})")
        }
    }

    object PosRightTop : ISystemCommand {
        override val exampleCommand: String= "/sd posrt"
        override val helpText: String = "set the ${ChatColor.GOLD}right top${ChatColor.RESET} position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("posrt") { p ->
            val plane = getPlayersStory(p).getPlane()
            val location = p.location
            plane.setRightTop(location.blockLocation())
            p.sendMessage("${SBPlugin.title}set ${ChatColor.GOLD}right top${ChatColor.RESET} to your current location ${ChatColor.GRAY}(${location.blockX}/${location.blockY}/${location.blockZ})")
        }
    }

    object PosLeftBottom : ISystemCommand {
        override val exampleCommand: String= "/sd poslb"
        override val helpText: String = "set the ${ChatColor.LIGHT_PURPLE}left bottom${ChatColor.RESET} position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("poslb") { p ->
            val plane = getPlayersStory(p).getPlane()
            val location = p.location
            plane.setLeftBottom(location.blockLocation())
            p.sendMessage("${SBPlugin.title}set ${ChatColor.LIGHT_PURPLE}left bottom${ChatColor.RESET} to your current location ${ChatColor.GRAY}(${location.blockX}/${location.blockY}/${location.blockZ})")
        }
    }

    object PosRightBottom : ISystemCommand {
        override val exampleCommand: String= "/sd posrb"
        override val helpText: String = "set the ${ChatColor.AQUA}right bottom${ChatColor.RESET} position of the plane where you are"
        override val baseTree = CommandEmptyLeaf("posrb") { p ->
            val plane = getPlayersStory(p).getPlane()
            val location = p.location
            plane.setRightBottom(location.blockLocation())
            p.sendMessage("${SBPlugin.title}set ${ChatColor.AQUA}right bottom${ChatColor.RESET} to your current location ${ChatColor.GRAY}(${location.blockX}/${location.blockY}/${location.blockZ})")
        }
    }

    object CurveFactor : ISystemCommand {
        override val exampleCommand: String ="/sd curveFactor [value/Double]"
        override val helpText: String = "changes how extreme the curves of your plane are (0 as if there where no control points)"
        override val baseTree: ICommandBranch = CommandDoubleLeaf("curveFactor", 0.0, null, {p,f ->
            val plane = getPlayersStory(p).morphPlane
            if(plane != null){
                plane.curveFactor = f
                p.sendMessage("${SBPlugin.title}set curve factor to your $f")
            }
            else
                p.sendMessage("${SBPlugin.title}${ChatColor.RED}you have no plane")
        },{p ->
            val plane = getPlayersStory(p).morphPlane
            if(plane != null)
                p.sendMessage("${SBPlugin.title}curve factor is currently set to ${plane.curveFactor}")
           else
               p.sendMessage("${SBPlugin.title}${ChatColor.RED}you have no plane")
        })
    }

    object Font : ISystemCommand {
        override val exampleCommand: String = "/sb font [fontFile]"
        override val helpText: String = "to change the font you want to use to build with"
        override val baseTree: ICommandBranch = CommandStringLeaf("font", { FontFileSystem.getAllFiles(false) },
            { p, fileName ->
                val done = loadFont(p,fileName)
                if(done != null) p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}font has been changed to: $done")
                else p.sendMessage("${SBPlugin.title}${ChatColor.RED}cant find font file: ${ChatColor.WHITE}$fileName")
            },
            { p -> p.sendMessage("${SBPlugin.title}${loadFont(p,null)}") })
    }

    object Samples : ISystemCommand{
        override val exampleCommand: String = "/sb samples [amount/Int]"
        override val helpText: String = "change the amount of samples that are being used while building a structure"
        override val baseTree: ICommandBranch = CommandIntLeaf("samples", 10, null, { p, amount ->
            getPlayersStory(p).samples = amount
            p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}changed sample amount to: ${ChatColor.WHITE}$amount")
        },{p ->
            p.sendMessage("${SBPlugin.title}${ChatColor.WHITE}sample amount is currently: ${ChatColor.WHITE}${getPlayersStory(p).samples}")
        })

    }

}