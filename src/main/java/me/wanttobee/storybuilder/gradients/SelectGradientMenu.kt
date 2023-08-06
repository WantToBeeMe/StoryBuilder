package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.SBUtil
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

class SelectGradientMenu(private val gradient : Gradient, private val doneEffect : (Player)->Unit) : IInventoryMenu()  {
    constructor(player :Player, doneEffect: (Player) -> Unit) : this(StorySystem.getPlayersStory(player).currentGradient, doneEffect)
    override var inventory: Inventory = Bukkit.createInventory(null, 9, "block/gradient picker")
    private val gradientPicker = SBUtil.itemFactory(Material.CHEST, "${ChatColor.GOLD}Load a gradient", listOf("${ChatColor.GRAY}or click with a block for", "${ChatColor.GRAY}a single block action"))

    init{
        InventoryMenuSystem.addInventory(this)

        this.addLockedItem(7, separator)
        loadGradient()
        initLoadGradient()
        initDoneButton()
    }
    private fun loadGradient(){
        for(i in 0 until 4){
            val mat = gradient.getReal(i) ?: Material.AIR
            this.addLockedItem(2 + i, ItemStack(mat) )
        }
        if(gradient.size > 5){
            val extraMat =  SBUtil.itemFactory(Material.BOOK, "${ChatColor.GOLD}+${gradient.size - 4}", null)
            this.addLockedItem(6, extraMat )
        }
        else{
            val mat = gradient.getReal(4) ?: Material.AIR
            this.addLockedItem(6, ItemStack(mat) )
        }

        this.addLockedItem(1, separator)
    }

    private fun initLoadGradient(){
        this.addLockedItem(0, gradientPicker) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            GradientListMenu() {pickedPlayer,grad ->
                SelectGradientMenu(grad,doneEffect).open(pickedPlayer)
            }.open(pickingPlayer)
        }
    }

    private fun initDoneButton(){
        val item = SBUtil.itemFactory(Material.SLIME_BALL, "${ChatColor.GREEN}Done", null)
        this.addLockedItem(8, item) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            StorySystem.getPlayersStory(pickingPlayer).currentGradient = gradient
            this.closeViewers()
            doneEffect.invoke(pickingPlayer)
        }
    }

    override fun clickEvent(player: Player, event: InventoryClickEvent) {
        val item = event.currentItem ?: return

        if(item == gradientPicker){
            val mat = event.cursor?.type ?: Material.FEATHER
            if(mat.isBlock && mat != Material.AIR){
                SelectGradientMenu(Gradient("SingleBlock", arrayOf(mat)),doneEffect).open(player)
                event.isCancelled = true
                return
            }
        }
        super.clickEvent(player, event)
    }

    override fun closeEvent(player : Player, event : InventoryCloseEvent){
        InventoryMenuSystem.removeInventory(this)
    }



}