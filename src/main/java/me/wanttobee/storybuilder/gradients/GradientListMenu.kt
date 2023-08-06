package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GradientListMenu private constructor(private val selectEffect : (Player, Gradient) -> Unit, private val page : Int) : IInventoryMenu() {
    constructor(selectEffect: (Player, Gradient) -> Unit) : this(selectEffect,0)

    override var inventory: Inventory = Bukkit.createInventory(null, 6*9,"Gradient Selector")

    init{

        InventoryMenuSystem.addInventory(this)
        val gradientFiles = GradientFileSystem.getAllFiles(false)
        initHotBar(gradientFiles.size/5)
        val optionsArray : Array<Gradient?> = Array(5) {i ->
            val gradIndex = i + 5*page
            if(gradientFiles.size <= gradIndex)
                null
            else {
                val fileName = gradientFiles[gradIndex]
                GradientFileSystem.getGradient(fileName)
            }
        }
        initGradientOptions(optionsArray)
    }
    override fun closeEvent(player : Player, event : InventoryCloseEvent){
        val did =  InventoryMenuSystem.removeInventory(this)
    }

    private fun initHotBar(lastPage : Int){
        val bottomItem = SBUtil.itemFactory(Material.BLACK_STAINED_GLASS_PANE, "${ChatColor.GRAY}${page+1}/${lastPage+1}", null)
        if(page == 0) this.addLockedItem(5, 0, bottomItem)
        else{
            val arrow = SBUtil.itemFactory(Material.ARROW, "${ChatColor.YELLOW}Previous", null)
            this.addLockedItem(5,0,arrow) {player ->
                GradientListMenu(selectEffect, page-1).open(player)
            }
        }
        if(page == lastPage) this.addLockedItem(5,8,bottomItem)
        else{
            val arrow = SBUtil.itemFactory(Material.ARROW, "${ChatColor.YELLOW}Next", null)
            this.addLockedItem(5,8,arrow) {player ->
                GradientListMenu(selectEffect, page+1).open(player)
            }
        }
        for(i in 1 until 8)
            this.addLockedItem(5,i,bottomItem)
    }

    private fun initGradientOptions(options : Array<Gradient?>){
        for(gradientIndex in options.indices){
            val grad = options[gradientIndex] ?: continue
            val tag = SBUtil.itemFactory(Material.NAME_TAG, "${ChatColor.GOLD}${grad.name}", null)
            this.addLockedItem(gradientIndex,0,tag) {player ->
                selectEffect.invoke(player, grad)
                this.closeViewers()
            }
            for(gradBlockIndex in 0 until 7){
                val mat = grad.getReal(gradBlockIndex) ?: break
                this.addLockedItem(gradientIndex,gradBlockIndex+1, ItemStack(mat) )
            }

            if(grad.size >= 8){
                val extraMat =  SBUtil.itemFactory(Material.BOOK, "${ChatColor.GOLD}+${grad.size - 6}", null)
                this.addLockedItem(gradientIndex,8, extraMat)
                //{ player ->
                //    GradientDetailsMenu(grad) { player2 ->
                //        GradientListMenu(selectEffect,page).open(player2)
                //        SBPlugin.instance.logger.info("opened list")
                //    }.open(player)
                //    SBPlugin.instance.logger.info("opened Details")
                //}
            }
            else{
                val mat = grad.getReal(7)
                if(mat != null ) this.addLockedItem(gradientIndex,8, ItemStack(mat) )
            }
        }
    }
}