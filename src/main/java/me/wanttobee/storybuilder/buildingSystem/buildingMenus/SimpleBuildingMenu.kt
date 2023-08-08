package me.wanttobee.storybuilder.buildingSystem.buildingMenus

import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class SimpleBuildingMenu(owner : Player, doneEffect : ()->Unit) : IBuildMenu( owner,doneEffect) {
    override var inventory: Inventory = Bukkit.createInventory(null, 9, "block/gradient picker")

    override fun openThisWindowAgain(player: Player) { SimpleBuildingMenu(owner,doneEffect).open(player) }
    override fun reloadGradient() { this.loadPrimaryGradient(0,0,6) }

    init{
        InventoryMenuSystem.addInventory(this)
        this.reloadGradient()
        this.addLockedItem(7, separator)
        setDoneButton(0,8)
    }

}