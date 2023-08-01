package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.systems.FontSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.awt.Font

class PlayersStory(private val owner : Player) {
    val blockRecorder = BlockRecorder()
    private var fontPath = ""
    private var font : Font? = null
    var morphPlane : MorphPlane? = null
        private set

    fun getPlane() : MorphPlane{
        if(morphPlane == null)
            morphPlane = MorphPlane(owner)
        return morphPlane!!
    }
    fun clear(){
        if(morphPlane != null)
            morphPlane!!.undoPlacePoints()
    }
    fun loadFont(newFontPath: String) {
        val newFont = FontSystem.getFont(newFontPath)
        if(newFont == null){
            owner.sendMessage("${ChatColor.RED}Cant find font file: ${ChatColor.GRAY}$newFontPath")
            return
        }
        font = newFont
        fontPath  = newFontPath
        owner.sendMessage("${ChatColor.GREEN}font has been changed to:")
        currentFontMessage()
    }
    fun currentFontMessage(){
        if(font == null) owner.sendMessage("${ChatColor.RED}Currently no font active")
        else owner.sendMessage("${ChatColor.WHITE}${fontPath.removeSuffix(".ttf")}> ${ChatColor.GOLD}${font!!.fontName}")
    }

    fun tick() {
        if (morphPlane != null && owner.inventory.itemInMainHand.type == Material.FEATHER)
            morphPlane!!.tick()
    }

    private var didDrop = false
    private var editing : String? = null
    fun onFeatherInteract(event: PlayerInteractEvent){
        if(didDrop) {
            didDrop = false
            event.isCancelled = true
            return
        }
        if(morphPlane == null){
            owner.sendMessage("${SBPlugin.title}created a new plane at your location")
            owner.sendMessage("${ChatColor.GOLD}- ${ChatColor.WHITE}drop the feather to clear the plane")
            owner.sendMessage("${ChatColor.GOLD}- ${ChatColor.WHITE}right click on points to move them")
            owner.sendMessage("${ChatColor.GOLD}- ${ChatColor.WHITE}left click to add or remove points")
            morphPlane = MorphPlane(owner)
            event.isCancelled = true
            return
        }

        val action = event.action
        val blockClicked = event.clickedBlock?.location ?: owner.location
        if(action == Action.LEFT_CLICK_BLOCK ||action == Action.LEFT_CLICK_AIR){

        }

        if(action == Action.RIGHT_CLICK_BLOCK ||action == Action.RIGHT_CLICK_AIR){
           if(editing == null){
               val newlyEditing = morphPlane!!.startEditing(blockClicked)
               if(newlyEditing != null){
                   editing = newlyEditing
                   owner.sendMessage("${SBPlugin.title}start editing: $newlyEditing")
               }
               else{
                   if(morphPlane!!.pointsPlaced)  morphPlane!!.undoPlacePoints()
                   else  morphPlane!!.placePoints()
               }
               event.isCancelled = true
               return
           }
            else{
               morphPlane!!.stopEditing(blockClicked)
               owner.sendMessage("${SBPlugin.title}finished editing: $editing")
               editing = null
               event.isCancelled = true
               return
           }
        }
    }

    fun onFeatherDrop(event : PlayerDropItemEvent){
        if(morphPlane != null){
            morphPlane!!.undoPlacePoints()
            morphPlane = null
            owner.sendMessage("${SBPlugin.title}plane has been cleared")
            event.isCancelled = true
            didDrop = true
            return
        }
    }
}