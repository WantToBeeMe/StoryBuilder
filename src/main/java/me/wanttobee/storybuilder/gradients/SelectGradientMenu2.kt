package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.inventoryMenus.IBuildMenu
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.systems.playerStory.StorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SelectGradientMenu2(owner : Player, doneEffect : (Player)->Unit) : IBuildMenu(0,0,6 ,owner,doneEffect) {
    override var inventory: Inventory = Bukkit.createInventory(null, 9, "block/gradient picker")
    override fun openThisWindowAgain(player: Player) {
        SelectGradientMenu2(owner,doneEffect).open(player)
    }

    init{
        InventoryMenuSystem.addInventory(this)

        this.addLockedItem(7, separator)
        this.loadGradient()
        initDoneButton()
    }

    private fun initDoneButton(){
        val item = SBUtil.itemFactory(Material.SLIME_BALL, "${ChatColor.GREEN}Done", null)
        this.addLockedItem(8, item) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            this.closeViewers()
            doneEffect.invoke(pickingPlayer)
        }
    }
}