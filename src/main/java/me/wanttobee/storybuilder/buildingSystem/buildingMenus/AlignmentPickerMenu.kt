package me.wanttobee.storybuilder.buildingSystem.buildingMenus

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class AlignmentPickerMenu(owner : Player,private val closeEvent : () -> Unit) : IInventoryMenu() {
    override var inventory: Inventory = Bukkit.createInventory(null, InventoryType.DROPPER, "Alignment Selector")
    private var effectInvoked = false
    init{
        InventoryMenuSystem.addInventory(this)
        val currentAlignment = StorySystem.getPlayersStory(owner).alignment
        val alignments = arrayOf(
                Pair("Left Top",Alignment.LEFT_TOP ),
                Pair("Centered Top",Alignment.CENTERED_TOP ),
                Pair("Right Top",Alignment.RIGHT_TOP ),

                Pair("Left Centered",Alignment.CENTERED_LEFT ),
                Pair("Centered",Alignment.CENTERED ),
                Pair("Right Centered",Alignment.CENTERED_RIGHT ),

                Pair("Left Bottom",Alignment.LEFT_BOTTOM ),
                Pair("Centered Bottom",Alignment.CENTERED_BOTTOM ),
                Pair("Right Bottom",Alignment.RIGHT_BOTTOM ),
        )
        for(i in alignments.indices){
            val pair = alignments[i]
            val item = if(currentAlignment != pair.second) SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.YELLOW}Set Alignment to ${pair.first}", null)
            else SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}Currently ${pair.first}", null)
            this.addLockedItem(i, item) {player ->
                if(currentAlignment != pair.second){
                    StorySystem.getPlayersStory(player).alignment = pair.second
                    effectInvoked = true
                    closeEvent.invoke()
                }
            }
        }
    }

    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        InventoryMenuSystem.removeInventory(this)
        if(!effectInvoked) closeEvent.invoke()
    }
}