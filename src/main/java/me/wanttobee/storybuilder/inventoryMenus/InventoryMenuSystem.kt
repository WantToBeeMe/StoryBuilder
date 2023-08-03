package me.wanttobee.storybuilder.inventoryMenus

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

object InventoryMenuSystem : Listener {
    private val inventories : MutableList<IInventoryMenu> = mutableListOf()

    fun addInventory(inv : IInventoryMenu) : Boolean{
        val contains = inventories.contains(inv)
        if(!contains)
            inventories.add(inv)
        return !contains
    }
    fun removeInventory(inv : IInventoryMenu) : Boolean{
        return inventories.remove(inv)
    }

    @EventHandler
    fun iClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked as? Player ?: return

        for(inv in inventories){
            if(inv.isThisInventory(clickedInventory)){
                inv.clickEvent(player, event)
                return
            }
            else if(inv.isThisInventory(player.openInventory.topInventory)){
                inv.topClickEvent(player, event)
                return
            }
        }

    }
    @EventHandler
    fun iDrag(event: InventoryDragEvent) {
        val clickedInventory = event.inventory
        val player = event.whoClicked as? Player ?: return

        for(inv in inventories){
            if(inv.isThisInventory(clickedInventory)){
                inv.dragEvent(player, event)
                return
            }
            else if(inv.isThisInventory(player.openInventory.topInventory)){
                inv.topDragEvent(player, event)
                return
            }
        }
    }
    @EventHandler
    fun iClose(event: InventoryCloseEvent) {
        val clickedInventory = event.inventory
        val player = event.player as? Player ?: return

        for (inv in inventories) {
            if (inv.isThisInventory(clickedInventory)) {
                inv.closeEvent(player, event)
                return
            }
        }
    }

}