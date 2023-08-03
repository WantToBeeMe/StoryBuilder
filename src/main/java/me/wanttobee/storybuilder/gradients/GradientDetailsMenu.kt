package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GradientDetailsMenu(grad : Gradient,private val closeEvent: (Player) -> Unit) : IInventoryMenu() {
    override var inventory: Inventory = Bukkit.createInventory(null,(grad.size/9+1)*9 , grad.name)
    init{
        InventoryMenuSystem.addInventory(this)
        for(i in 0..grad.size){
            val mat = grad.getReal(i) ?: Material.AIR
            this.addLockedItem(i, ItemStack(mat))
        }
    }

    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        InventoryMenuSystem.removeInventory(this)
        closeEvent.invoke(player)
    }

}