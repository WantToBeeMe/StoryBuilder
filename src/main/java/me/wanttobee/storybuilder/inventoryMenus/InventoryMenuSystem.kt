package me.wanttobee.storybuilder.inventoryMenus

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandEmptyLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch
import org.bukkit.ChatColor
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
                inv.bottomClickEvent(player, event)
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
                inv.bottomDragEvent(player, event)
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



    fun debugText(commander : Player) {
        commander.sendMessage("${SBPlugin.title}${ChatColor.YELLOW}active menu's:")
        if(inventories.isEmpty())
            commander.sendMessage("${ChatColor.GOLD}none open!!")
        for(inv in inventories)
            commander.sendMessage("${ChatColor.GOLD}- ${ChatColor.WHITE}${inv::class.simpleName} ${ChatColor.GRAY}(${inv.amountViewers()} open)")
    }
    object MenuDebug : ISystemCommand{
        override val exampleCommand: String ="/sb menuDebug"
        override val helpText: String = "if this is active, then i forgot to deactivate it :P, (is for checking if all the inventorys works as they should)"
        override val baseTree: ICommandBranch = CommandEmptyLeaf("menuDebug") {player -> debugText(player)}
    }

}