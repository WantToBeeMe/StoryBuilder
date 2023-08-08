package me.wanttobee.storybuilder.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.buildingSystem.ClampMode
import me.wanttobee.storybuilder.buildingSystem.ClampSides
import me.wanttobee.storybuilder.gradients.Gradient
import me.wanttobee.storybuilder.gradients.GradientMakerMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.morphPlane.MorphPlane
import me.wanttobee.storybuilder.morphPlane.MorphPlaneMenu
import me.wanttobee.storybuilder.buildingSystem.buildingMenus.Alignment
import me.wanttobee.storybuilder.buildingSystem.font.FontFileSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.awt.Font

class PlayersStory(private val owner : Player) {
    private val blockRecorder = BlockRecorder()
    val gradientMaker = GradientMakerMenu()
    //personal settings
    private var fontPath = ""
    private var font : Font? = null
    var samples = 100
    var primaryGradient : Gradient = Gradient("primary", arrayOf(Material.STONE) )
    var secondaryGradient : Gradient = Gradient("secondary", arrayOf(Material.COBBLESTONE) )
    var morphPlane : MorphPlane? = null
    var alignment : Alignment = Alignment.CENTERED
    var fontFill : Boolean = true
    var fontOutOfBound : Boolean = false
    var fontClampSide : ClampSides = ClampSides.LEFT_OR_TOP
    var fontClampMode : ClampMode = ClampMode.AUTO
    var useFontSize : Boolean = false
    var fontLogicBoundingBox : Boolean = true
    var fontSize = 50

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