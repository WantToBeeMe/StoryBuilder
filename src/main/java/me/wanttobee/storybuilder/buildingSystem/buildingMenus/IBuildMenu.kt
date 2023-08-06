package me.wanttobee.storybuilder.buildingSystem.buildingMenus

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.gradients.Gradient
import me.wanttobee.storybuilder.gradients.GradientListMenu
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

abstract class IBuildMenu(protected val owner :Player, protected val doneEffect : ()->Unit) : IInventoryMenu() {
    companion object{
        private val gradientPicker = SBUtil.itemFactory(Material.CHEST, "${ChatColor.GOLD}Load a gradient", listOf("${ChatColor.GRAY}or click with a block for", "${ChatColor.GRAY}a single block action"))
        private val doneItem = SBUtil.itemFactory(Material.SLIME_BALL, "${ChatColor.GREEN}Done", null)
    }

    override fun clickEvent(player: Player, event: InventoryClickEvent) {
        val item = event.currentItem ?: run {
            event.isCancelled = true
            return
        }
        if(item == gradientPicker){
            val mat = event.cursor?.type ?: Material.FEATHER
            if(mat.isBlock && mat != Material.AIR){
                StorySystem.getPlayersStory(player).currentGradient = Gradient("SingleBlock", arrayOf(mat))
                reloadGradient()
                event.isCancelled = true
                return
            }
        }
        super.clickEvent(player, event)
    }

    override fun bottomClickEvent(player : Player, event : InventoryClickEvent){
        val item = event.currentItem ?: return
        val itemWithoutStackSize = item.clone()
        itemWithoutStackSize.amount = 1

        if(lockedItems.contains(itemWithoutStackSize)){
            if(event.isShiftClick)
                event.isCancelled = true
            else if(event.isLeftClick){
                event.cursor = item
                event.isCancelled = true
            }
        }
    }
    protected fun loadGradient(gradientSelectorRow : Int, columnStart : Int, columnEnd : Int,){
        this.addLockedItem(gradientSelectorRow,columnStart, gradientPicker) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            GradientListMenu() {pickedPlayer,grad ->
                StorySystem.getPlayersStory(pickedPlayer).currentGradient = grad
                openThisWindowAgain(pickedPlayer)
            }.open(pickingPlayer)
        }
        this.addLockedItem(gradientSelectorRow, columnStart+1, separator)

        val gradient = StorySystem.getPlayersStory(owner).currentGradient
        for(i in 0 until columnEnd-columnStart - 2){
            val mat = gradient.getReal(i) ?: Material.AIR
            this.addLockedItem(gradientSelectorRow,i+columnStart + 2 , ItemStack(mat) )
        }
        if(gradient.size > columnEnd-columnStart - 1){
            val extraMat =  SBUtil.itemFactory(Material.BOOK, "${ChatColor.GOLD}+${gradient.size -( columnEnd-columnStart - 2)}", null)
            this.addLockedItem(gradientSelectorRow,columnEnd, extraMat )
        }
        else{
            val mat = gradient.getReal(4) ?: Material.AIR
            this.addLockedItem(gradientSelectorRow,columnEnd, ItemStack(mat) )
        }
    }

    protected fun setDoneButton(row : Int, column: Int){
        this.addLockedItem(row,column, doneItem) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            this.closeViewers()
            doneEffect.invoke()
        }
    }


    abstract fun openThisWindowAgain(player : Player)
    abstract fun reloadGradient()

    override fun closeEvent(player : Player, event : InventoryCloseEvent){
        InventoryMenuSystem.removeInventory(this)
    }

}