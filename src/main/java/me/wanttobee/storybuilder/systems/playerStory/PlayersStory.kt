package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.gradients.Gradient
import me.wanttobee.storybuilder.gradients.GradientMakerMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.morphPlane.MorphPlane
import me.wanttobee.storybuilder.morphPlane.MorphPlaneMenu
import me.wanttobee.storybuilder.systems.FontFileSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.awt.Font

class PlayersStory(private val owner : Player) {
    private val blockRecorder = BlockRecorder()
    private var fontPath = ""
    private var font : Font? = null
    var samples = 100
    val gradientMaker = GradientMakerMenu()
    var currentGradient : Gradient = Gradient("default", arrayOf(Material.STONE) )//GradientFileSystem.getGradient( GradientFileSystem.getAllFiles().first() )!!
    var morphPlane : MorphPlane? = null

    fun runBlockRecorderAsync(task : (BlockRecorder) -> Unit){
        blockRecorder.runRecorderAsync(task)
    }
    fun runBlockRecorderSynced(task: (BlockRecorder) -> Unit){
        blockRecorder.runRecorderSynced(task)
    }

    fun undo(amount : Int) { blockRecorder.undo(amount,owner) }
    fun redo(amount : Int) { blockRecorder.redo(amount,owner) }

    fun getPlane() : MorphPlane {
        if(morphPlane == null){
            owner.sendMessage("${SBPlugin.title}created a new plane")
            morphPlane = MorphPlane(owner.world)
        }
        return morphPlane!!
    }
    fun deletePlane(){
        if(morphPlane != null){
            owner.sendMessage("${SBPlugin.title}deleted current plane")
            morphPlane = null
        }
    }

    fun clear(){
        InventoryMenuSystem.removeInventory(gradientMaker)
    }

    fun getCurrentFont() : Font?{
        return font
    }

    fun loadFont(newFontPath: String) : String? {
        font = FontFileSystem.getFont(newFontPath) ?: return null
        fontPath  = newFontPath
        return currentFontMessage()
    }
    fun currentFontMessage() : String{
        return if(font == null) "${ChatColor.RED}Currently no font active"
        else "${ChatColor.WHITE}${fontPath.removeSuffix(".ttf")}> ${ChatColor.GOLD}${font!!.fontName}"
    }

    fun tick() {
        if (morphPlane != null && (owner.inventory.itemInMainHand.type == Material.FEATHER || owner.inventory.itemInOffHand.type == Material.FEATHER))
            morphPlane!!.tick()
    }

    fun onFeatherInteract(event: PlayerInteractEvent){
        val blockClicked = event.clickedBlock?.location ?: owner.location
        if(morphPlane == null || morphPlane?.world == owner.world)
            MorphPlaneMenu(owner,blockClicked).open(owner)
        else
            owner.sendMessage("${SBPlugin.title}${ChatColor.RED}you must be in the same dimension/world to perform this action:${ChatColor.GRAY} ${morphPlane?.world?.name}")
        event.isCancelled = true
    }
}

//fun onFeatherDrop(event : PlayerDropItemEvent){}