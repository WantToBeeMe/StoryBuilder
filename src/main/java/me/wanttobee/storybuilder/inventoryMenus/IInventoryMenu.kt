package me.wanttobee.storybuilder.inventoryMenus

import me.wanttobee.storybuilder.SBUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

abstract class IInventoryMenu {
    companion object{
        val separator = SBUtil.itemFactory(Material.BLACK_STAINED_GLASS_PANE, " ", null)
    }
    protected abstract var inventory: Inventory
    protected val lockedItems : MutableList<ItemStack> = mutableListOf()
    protected val clickEvents : MutableMap<ItemStack, (Player) -> Unit> = mutableMapOf()

    fun amountViewers() : Int{ return inventory.viewers.size }

    fun isThisInventory(check :Inventory?) : Boolean{
        return check == inventory
    }

    open fun bottomClickEvent(player : Player, event : InventoryClickEvent){
        val item = event.currentItem ?: return
        val itemWithoutStackSize = item.clone()
        itemWithoutStackSize.amount = 1

        if(lockedItems.contains(itemWithoutStackSize)){
            if(event.isShiftClick || event.isLeftClick)
                event.isCancelled = true
        }
    }
    open fun clickEvent(player : Player, event : InventoryClickEvent){
        val item = event.currentItem ?: return

        if(lockedItems.contains(item)){
            if(clickEvents.containsKey(item)){
                clickEvents[item]!!.invoke(player)
            }
            event.isCancelled = true
        }
    }

    open fun bottomDragEvent(player : Player, event: InventoryDragEvent){}
    open fun dragEvent(player : Player, event: InventoryDragEvent){}

    open fun closeEvent(player : Player, event : InventoryCloseEvent){}

    fun open(player : Player){
        player.openInventory(inventory)
    }
    fun closeViewers(){
        for (viewerID in inventory.viewers.indices) {
            if (inventory.viewers.size > viewerID
                && inventory.viewers[viewerID] is Player
                && inventory.viewers[viewerID].openInventory.topInventory == inventory) {
                inventory.viewers[viewerID].closeInventory()
            }
        }
    }
    fun addLockedItem(slot: Int, item:ItemStack, event:((Player) -> Unit)? = null){
        inventory.setItem(slot, item)
        lockedItems.add(item)
        if(event != null) clickEvents[item] = event
    }
    fun addLockedItem(row : Int, column : Int, item : ItemStack, event :((Player) -> Unit)? = null){
        return addLockedItem(row*9 + column, item,event)
    }

    fun removeItem(item : ItemStack){
        lockedItems.remove(item)
        clickEvents.remove(item)
    }


}