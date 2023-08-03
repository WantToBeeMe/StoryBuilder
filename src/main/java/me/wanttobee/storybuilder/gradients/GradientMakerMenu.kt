package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GradientMakerMenu : IInventoryMenu()  {
    override var inventory: Inventory = Bukkit.createInventory(null,6*9,"Gradient Maker")
    private var name = ""

    init{
        InventoryMenuSystem.addInventory(this)
        initStatic()
        initSaveButton()
        initLoadCopy()
        initChangeName( generateNewName() )
    }

    companion object{
        private fun generateNewName() : String {
            var index = 1
            var newName = "NewGradient"
            var existingNames= GradientFileSystem.getAllFiles(false)
            while(existingNames.contains(newName)){
                newName = "NewGradient_${index++}"
            }
            return newName
        }
    }

    private fun initStatic(){
        for(i in 0 until 9)
            this.addLockedItem(1, i, separator)

        this.addLockedItem(0, 0, SBUtil.itemFactory(Material.DIORITE, "${ChatColor.WHITE}Light to Dark",null))
        this.addLockedItem(0, 1, SBUtil.itemFactory(Material.ANDESITE, "${ChatColor.GRAY}Light to Dark",null))
        this.addLockedItem(0, 2, SBUtil.itemFactory(Material.DEEPSLATE, "${ChatColor.DARK_GRAY}Light to Dark",null))
        this.addLockedItem(0, 3, SBUtil.itemFactory(Material.CRACKED_DEEPSLATE_TILES, "${ChatColor.BLACK}Light to Dark",null))

        this.addLockedItem(0, 4, separator)
        this.addLockedItem(0, 7, separator)
    }

    private fun initSaveButton(){
        val item = SBUtil.itemFactory(Material.SLIME_BALL, "${ChatColor.GREEN}Save", listOf("${ChatColor.GRAY}will be there, even","${ChatColor.GRAY}if the server reloads"))
        this.addLockedItem(0, 8, item) {player ->
            val done = GradientFileSystem.saveGradient(name, getCurrentGradient() )
            if(done){
                player.sendMessage("${SBPlugin.title}${ChatColor.GREEN}saved the gradient ${ChatColor.GRAY}$name")
                reset()
            }
            else player.sendMessage("${SBPlugin.title}${ChatColor.RED}cant save this gradient, something is wrong")

        }
    }

    private fun initLoadCopy(){
        val item = SBUtil.itemFactory(Material.CHEST, "${ChatColor.GOLD}Load a copy", listOf("${ChatColor.GRAY}will remove your progress", "${ChatColor.GRAY}won't edit the original"))
        this.addLockedItem(0, 6, item) { pickingPlayer -> //the pickingPlayer and pickedPlayer are the same lol
            GradientListMenu() {pickedPlayer,grad ->
                loadGradiant(grad)
                this.open(pickedPlayer)
            }.open(pickingPlayer)
        }
    }

    private fun initChangeName(newName : String){
        this.removeItem(SBUtil.itemFactory(Material.NAME_TAG, "${ChatColor.GOLD}Name: ${ChatColor.WHITE}$name", null)) //"${ChatColor.GRAY}click to edit"
        name = newName
        val item = SBUtil.itemFactory(Material.NAME_TAG, "${ChatColor.GOLD}Name: ${ChatColor.WHITE}$name", null ) //"${ChatColor.GRAY}click to edit"
        this.addLockedItem(0,5, item)
    }

    private fun getCurrentGradient() : Array<Material>{
        val materialList = mutableListOf<Material>()
        for(i in 9*2 until 9*6){
            val item = inventory.getItem(i) ?: continue
            materialList.add(item.type)
        }
        return materialList.toTypedArray()
    }

    private fun loadGradiant(grad : Gradient){
        for(i in 0 until 4*9){
            val mat = grad.getReal(i) ?: Material.AIR
            inventory.setItem(9*2 + i, ItemStack(mat))
        }
    }

    private fun reset(){
        closeViewers()
        initChangeName(  generateNewName()  )
        for(i in 0 until 4*9)
            inventory.setItem(9*2 + i, ItemStack(Material.AIR))
    }

}