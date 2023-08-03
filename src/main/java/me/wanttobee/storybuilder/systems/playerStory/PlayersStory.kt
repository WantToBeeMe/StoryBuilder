package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.gradients.Gradient
import me.wanttobee.storybuilder.gradients.GradientFileSystem
import me.wanttobee.storybuilder.gradients.GradientMakerMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.systems.FontFileSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.awt.Font

class PlayersStory(private val owner : Player) {
    private val blockRecorder = BlockRecorder()
    private var fontPath = ""
    private var font : Font? = null
    val gradientMaker = GradientMakerMenu()
    var currentGradient : Gradient = Gradient("default", arrayOf(Material.STONE) )//GradientFileSystem.getGradient( GradientFileSystem.getAllFiles().first() )!!
    var morphPlane : MorphPlane? = null
        private set

    fun startBlockRecorder() : BlockRecorder{
        if(morphPlane != null) morphPlane!!.undoPlacePoints()
        blockRecorder.start()
        return blockRecorder
    }
    fun undo(amount : Int) : Boolean{ return blockRecorder.undo(amount) }
    fun redo(amount : Int) : Boolean{ return blockRecorder.redo(amount) }

    fun getPlane() : MorphPlane{
        if(morphPlane == null)
            morphPlane = MorphPlane(owner)
        return morphPlane!!
    }
    fun clear(){
        if(morphPlane != null)
            morphPlane!!.undoPlacePoints()
        InventoryMenuSystem.removeInventory(gradientMaker)
    }

    fun loadFont(newFontPath: String) {
        val newFont = FontFileSystem.getFont(newFontPath)
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
    private var removedBlock = false
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
            if(editing != null){
                owner.sendMessage("${SBPlugin.title}${ChatColor.RED}make sure you are not editing a point when adding or removing control points")
                event.isCancelled = true
                return
            }
            SBPlugin.instance.logger.info("left click!!!")
            val removed = morphPlane!!.removeControlPoint(blockClicked)
            if(removed != null){
                if(removed == "Corner") owner.sendMessage("${SBPlugin.title}${ChatColor.RED}Cant remove a corner point")
                else owner.sendMessage("${SBPlugin.title}removed point: $removed ${ChatColor.GRAY}(${blockClicked.blockX}/${blockClicked.blockY}/${blockClicked.blockZ})")
                removedBlock = true
                event.isCancelled = true
                return
            }else if(!removedBlock){
                val added = morphPlane!!.addControlPoint(blockClicked)
                owner.sendMessage("${SBPlugin.title}added point: $added ${ChatColor.GRAY}(${blockClicked.blockX}/${blockClicked.blockY}/${blockClicked.blockZ})")
                event.isCancelled = true
                return
            }
            removedBlock = false
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
               owner.sendMessage("${SBPlugin.title}finished editing: $editing ${ChatColor.GRAY}(${blockClicked.blockX}/${blockClicked.blockY}/${blockClicked.blockZ})")
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