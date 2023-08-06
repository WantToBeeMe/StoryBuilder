package me.wanttobee.storybuilder.morphPlane

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MorphPlaneMenu(owner : Player,location : Location) : IInventoryMenu() {
    override var inventory: Inventory = Bukkit.createInventory(null, 3*9, "Plane Editor ${ChatColor.GRAY}you: (${location.blockX},${location.blockY},${location.blockZ})")
    private val location : Location

    private val leftRightClickEvent : MutableMap<ItemStack, Pair<(Player) -> Unit,(Player) -> Unit>> = mutableMapOf()

    init{
        this.location = location.blockLocation()
        InventoryMenuSystem.addInventory(this)
        val story = StorySystem.getPlayersStory(owner)
        val plane = story.morphPlane
        initStatic()
        initCorners(plane)
        initControl(plane)
        initButtons(plane)
        initStatus(plane)
    }

    private fun initStatic(){
        this.addLockedItem(0,3, separator)
        this.addLockedItem(0,5, separator)
        for(i in 3 until 9)
            this.addLockedItem(1,i, separator)
        this.addLockedItem(2,3, separator)

        val clearAllItem = SBUtil.itemFactory(Material.BARRIER, "${ChatColor.RED}Clear Plane", null)
        this.addLockedItem(1,1, clearAllItem){player ->
            StorySystem.getPlayersStory(player).deletePlane()
            this.closeViewers()
        }
    }

    private fun initCorners(plane : MorphPlane?){
        val leftTop = if(plane?.leftTop != null) SBUtil.itemFactory(Material.LIME_CONCRETE, "${ChatColor.GREEN}Change LeftTop","${ChatColor.DARK_GRAY}Currently (${plane.leftTop!!.blockX},${plane.leftTop!!.blockY},${plane.leftTop!!.blockZ})")
        else SBUtil.itemFactory(Material.LIME_STAINED_GLASS, "${ChatColor.GREEN}Set LeftTop", "${ChatColor.DARK_GRAY}Currently not set")

        val rightTop = if(plane?.rightTop != null) SBUtil.itemFactory(Material.ORANGE_CONCRETE, "${ChatColor.GOLD}Change rightTop","${ChatColor.DARK_GRAY}Currently (${plane.rightTop!!.blockX},${plane.rightTop!!.blockY},${plane.rightTop!!.blockZ})")
        else SBUtil.itemFactory(Material.ORANGE_STAINED_GLASS, "${ChatColor.GOLD}Set rightTop", "${ChatColor.DARK_GRAY}Currently not set")

        val leftBottom = if(plane?.leftBottom != null) SBUtil.itemFactory(Material.MAGENTA_CONCRETE, "${ChatColor.LIGHT_PURPLE}Change leftBottom","${ChatColor.DARK_GRAY}Currently (${plane.leftBottom!!.blockX},${plane.leftBottom!!.blockY},${plane.leftBottom!!.blockZ})")
        else SBUtil.itemFactory(Material.MAGENTA_STAINED_GLASS, "${ChatColor.LIGHT_PURPLE}Set leftBottom", "${ChatColor.DARK_GRAY}Currently not set")

        val rightBottom = if(plane?.rightBottom != null) SBUtil.itemFactory(Material.CYAN_CONCRETE, "${ChatColor.AQUA}Change rightBottom","${ChatColor.DARK_GRAY}Currently (${plane.rightBottom!!.blockX},${plane.rightBottom!!.blockY},${plane.rightBottom!!.blockZ})")
        else SBUtil.itemFactory(Material.CYAN_STAINED_GLASS, "${ChatColor.AQUA}Set rightBottom", "${ChatColor.DARK_GRAY}Currently not set")

        this.addLockedItem(0,0, leftTop) {player ->
            StorySystem.getPlayersStory(player).getPlane().leftTop = location
            this.closeViewers()
        }

        this.addLockedItem(0,2, rightTop) {player ->
            StorySystem.getPlayersStory(player).getPlane().rightTop = location
            this.closeViewers()
        }

        this.addLockedItem(2,0, leftBottom) {player ->
            StorySystem.getPlayersStory(player).getPlane().leftBottom = location
            this.closeViewers()
        }

        this.addLockedItem(2,2, rightBottom) {player ->
            StorySystem.getPlayersStory(player).getPlane().rightBottom = location
            this.closeViewers()
        }
    }

    private fun initControl(plane : MorphPlane?){
        if(plane == null || !plane.isComplete()){
            val incompleteItem = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.GRAY}Add Control Point", listOf("${ChatColor.DARK_GRAY}You must complete the corner points","${ChatColor.DARK_GRAY}before you can add Control Points"))
            this.addLockedItem(0, 1 ,incompleteItem)
            this.addLockedItem(1, 0 ,incompleteItem)
            this.addLockedItem(1, 2 ,incompleteItem)
            this.addLockedItem(2, 1 ,incompleteItem)
        }
        else{
            val topList = plane.getControlPoints("top")
            val topItem = if(topList.size < 26)SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}${if(topList.isEmpty()) "Add" else "Edit"} Top Control Point", "${ChatColor.GRAY}${topList.size}/26")
            else SBUtil.itemFactory(Material.YELLOW_CONCRETE, "${ChatColor.YELLOW}Remove a Top Control Point", "${ChatColor.GRAY}${topList.size}/26")

            val bottomList = plane.getControlPoints("bottom")
            val bottomItem = if(bottomList.size < 26)SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}${if(topList.isEmpty()) "Add" else "Edit"} Bottom Control Point", "${ChatColor.GRAY}${bottomList.size}/26")
            else SBUtil.itemFactory(Material.YELLOW_CONCRETE, "${ChatColor.YELLOW}Remove a Bottom Control Point", "${ChatColor.GRAY}${bottomList.size}/26")

            val leftList = plane.getControlPoints("left")
            val leftItem = if(leftList.size < 26)SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}${if(topList.isEmpty()) "Add" else "Edit"} Left Control Point", "${ChatColor.GRAY}${leftList.size}/26")
            else SBUtil.itemFactory(Material.YELLOW_CONCRETE, "${ChatColor.YELLOW}Remove a Left Control Point", "${ChatColor.GRAY}${leftList.size}/26")

            val rightList = plane.getControlPoints("right")
            val rightItem = if(rightList.size < 26)SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}${if(topList.isEmpty()) "Add" else "Edit"} Right Control Point", "${ChatColor.GRAY}${rightList.size}/26")
            else SBUtil.itemFactory(Material.YELLOW_CONCRETE, "${ChatColor.YELLOW}Remove a Right Control Point", "${ChatColor.GRAY}${rightList.size}/26")

            this.addLockedItem(0, 1 ,topItem){player ->
                if(topList.isEmpty()) {
                    plane.addControlPoints("top", 0, location)
                    this.closeViewers()
                }
                else
                    ControlPointMenu(player,location,"top", topList.size >= 26).open(player)
            }
            this.addLockedItem(1, 0 ,leftItem){player ->
                if(leftList.isEmpty()) {
                    plane.addControlPoints("left", 0, location)
                    this.closeViewers()
                }
                else
                    ControlPointMenu(player,location,"left", leftList.size >= 26).open(player)
            }
            this.addLockedItem(1, 2 ,rightItem){player ->
                if(rightList.isEmpty()) {
                    plane.addControlPoints("right", 0, location)
                    this.closeViewers()
                }
                else
                    ControlPointMenu(player,location,"right", rightList.size >= 26).open(player)
            }
            this.addLockedItem(2, 1 ,bottomItem){player ->
                if(bottomList.isEmpty()) {
                    plane.addControlPoints("bottom", 0, location)
                    this.closeViewers()
                }
                else
                    ControlPointMenu(player,location,"bottom", bottomList.size >= 26).open(player)
            }
        }
    }

    private fun initButtons(plane : MorphPlane?){
        val emptyButton = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.GRAY}Empty", null)
        this.addLockedItem(2,6,emptyButton)
        this.addLockedItem(2,7,emptyButton)
        this.addLockedItem(2,8,emptyButton)
        if(plane == null || !plane.isComplete()){
            val rotate = SBUtil.itemFactory(Material.LIGHT_GRAY_CONCRETE_POWDER, "${ChatColor.GRAY}Rotate 90 deg", listOf("${ChatColor.DARK_GRAY}You must complete the ","${ChatColor.DARK_GRAY}corners before you can Rotate"))
            val flip = SBUtil.itemFactory(Material.LIGHT_GRAY_CONCRETE_POWDER, "${ChatColor.GRAY}Flip", listOf("${ChatColor.DARK_GRAY}You must complete the","${ChatColor.DARK_GRAY}corners before you can Flip"))
            this.addLockedItem(2,4,rotate)
            this.addLockedItem(2,5,flip)
        }else{
            val rotate = SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.GREEN}Rotate 90 deg", listOf("${ChatColor.GRAY}LefClick is Left Rotation","${ChatColor.GRAY}RightClick is Right Rotation"," ", "${ChatColor.DARK_GRAY}Note that this wont change the form","${ChatColor.DARK_GRAY}it is mainly used for directional actions","${ChatColor.DARK_GRAY}(like fonts)"))
            val flip = SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.GREEN}Flip", listOf("${ChatColor.GRAY}LefClick is Horizontal Flip","${ChatColor.GRAY}RightClick is Vertical Flip"," ", "${ChatColor.DARK_GRAY}Note that this wont change the form","${ChatColor.DARK_GRAY}it is mainly used for directional actions","${ChatColor.DARK_GRAY}(like fonts)"))
            this.addLockedItem(2,4,rotate)
            this.addLockedItem(2,5,flip)
            leftRightClickEvent[rotate] = Pair(
                {player -> plane.rotate(false); this.closeViewers()},
                {player -> plane.rotate(true); this.closeViewers() }
            )
            leftRightClickEvent[flip] = Pair(
                {player -> plane.flip(true); this.closeViewers() },
                {player -> plane.flip(false); this.closeViewers() }
            )
        }
    }

    private fun initStatus(plane : MorphPlane?){
        val completeIcon = if(plane != null && plane.isComplete()) SBUtil.itemFactory(Material.LIME_CANDLE, "${ChatColor.WHITE}Corners: ${ChatColor.GREEN}Done",  null) else
            SBUtil.itemFactory(Material.RED_CANDLE, "${ChatColor.WHITE}Corners: ${ChatColor.RED}Not Done",  listOf("${ChatColor.GRAY}All corners must be completed", "${ChatColor.GRAY}before you can do any building actions"))
        this.addLockedItem(0,4,completeIcon)

        if(plane == null || !plane.isComplete()){
            val horizontal = SBUtil.itemFactory(Material.LIGHT_GRAY_CANDLE, "${ChatColor.WHITE}Horizontal: ${ChatColor.GRAY}Unknown", listOf("${ChatColor.DARK_GRAY}You must complete the corners","${ChatColor.DARK_GRAY}before this can be calculated"))
            val vertical = SBUtil.itemFactory(Material.LIGHT_GRAY_CANDLE, "${ChatColor.WHITE}Vertical: ${ChatColor.GRAY}Unknown", listOf("${ChatColor.DARK_GRAY}You must complete the corners","${ChatColor.DARK_GRAY}before this can be calculated"))
            val degrees = SBUtil.itemFactory(Material.LIGHT_GRAY_CANDLE, "${ChatColor.WHITE}Degrees: ${ChatColor.GRAY}Unknown", listOf("${ChatColor.DARK_GRAY}You must complete the corners","${ChatColor.DARK_GRAY}before this can be calculated"))
            this.addLockedItem(0,6,horizontal)
            this.addLockedItem(0,7,vertical)
            this.addLockedItem(0,8,degrees)
        }else{
            val topLength : Double = plane.getSideLength("top")
            val bottomLength : Double = plane.getSideLength("bottom")
            val topToBottomPercentage = ((if(topLength < bottomLength) topLength/bottomLength else bottomLength/topLength) * 100).toInt()
            val horizontalLore = listOf("${ChatColor.GRAY}estimated top length: ${(topLength * 10).toInt()/10.0}","${ChatColor.GRAY}estimated bottom length: ${(bottomLength * 10).toInt()/10.0}")
            val horizontal = if(topToBottomPercentage < 50) SBUtil.itemFactory(Material.RED_CANDLE, "${ChatColor.WHITE}Horizontal: ${ChatColor.RED}$topToBottomPercentage%", horizontalLore)
                else if(topToBottomPercentage < 65)SBUtil.itemFactory(Material.ORANGE_CANDLE, "${ChatColor.WHITE}Horizontal: ${ChatColor.GOLD}$topToBottomPercentage%", horizontalLore)
                else if(topToBottomPercentage <80 )SBUtil.itemFactory(Material.YELLOW_CANDLE, "${ChatColor.WHITE}Horizontal: ${ChatColor.YELLOW}$topToBottomPercentage%", horizontalLore)
                else SBUtil.itemFactory(Material.LIME_CANDLE, "${ChatColor.WHITE}Horizontal: ${ChatColor.GREEN}$topToBottomPercentage%", horizontalLore)

            val leftLength : Double = plane.getSideLength("left")
            val rightLength : Double = plane.getSideLength("right")
            val leftToRightPercentage =((if(leftLength < rightLength) leftLength/rightLength else rightLength/leftLength) * 100).toInt()
            val verticalLore = listOf("${ChatColor.GRAY}estimated left length: ${(leftLength * 10).toInt()/10.0}","${ChatColor.GRAY}estimated right length: ${(rightLength * 10).toInt()/10.0}")
            val vertical = if(leftToRightPercentage <50) SBUtil.itemFactory(Material.RED_CANDLE, "${ChatColor.WHITE}Vertical: ${ChatColor.RED}$leftToRightPercentage%", verticalLore)
            else if(leftToRightPercentage <75)SBUtil.itemFactory(Material.ORANGE_CANDLE, "${ChatColor.WHITE}Vertical: ${ChatColor.GOLD}$leftToRightPercentage%", verticalLore)
            else if(leftToRightPercentage <90)SBUtil.itemFactory(Material.YELLOW_CANDLE, "${ChatColor.WHITE}Vertical: ${ChatColor.YELLOW}$leftToRightPercentage%", verticalLore)
            else SBUtil.itemFactory(Material.LIME_CANDLE, "${ChatColor.WHITE}Vertical: ${ChatColor.GREEN}$leftToRightPercentage%", verticalLore)
            this.addLockedItem(0,6,horizontal)
            this.addLockedItem(0,7,vertical)

            val degrees = SBUtil.itemFactory(Material.LIGHT_GRAY_CANDLE, "${ChatColor.WHITE}Degrees: ${ChatColor.GRAY}Not Implemented", "${ChatColor.DARK_GRAY}This status is not yet implemented")
            this.addLockedItem(0,8,degrees)
        }

    }


    override fun clickEvent(player: Player, event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if(lockedItems.contains(item)){
            if(clickEvents.containsKey(item))
                clickEvents[item]!!.invoke(player)
            else if(leftRightClickEvent.containsKey(item)){
                if(event.isLeftClick) leftRightClickEvent[item]!!.first.invoke(player)
                else if(event.isRightClick) leftRightClickEvent[item]!!.second.invoke(player)
            }
            event.isCancelled = true
        }
    }

    override fun bottomClickEvent(player: Player, event: InventoryClickEvent) {
        event.isCancelled = true
    }
    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        InventoryMenuSystem.removeInventory(this)
    }
}