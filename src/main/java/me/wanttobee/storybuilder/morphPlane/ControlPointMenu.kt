package me.wanttobee.storybuilder.morphPlane

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.inventoryMenus.IInventoryMenu
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class ControlPointMenu(private val owner : Player, private val location : Location, private val side: String, private val deletePoint : Boolean) : IInventoryMenu() {
    override lateinit var inventory: Inventory

    init {
        if(side != "top" && side != "bottom" && side != "left" && side != "right")
            initWrong()
        else
            initNormal()
        InventoryMenuSystem.addInventory(this)
    }
    private fun initWrong(){
        inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "${ChatColor.RED}Wrong Control point Type: $side")
        val errorItem = SBUtil.itemFactory(Material.BARRIER," ",null)
        for(i in 0 until 5)
            this.addLockedItem(i, errorItem)
    }

    private fun initNormal(){
        val plane = StorySystem.getPlayersStory(owner).getPlane()
        val from = when(side) {
            "top" -> SBUtil.itemFactory(Material.LIME_CONCRETE, "${ChatColor.GREEN}LeftTop", "${ChatColor.DARK_GRAY}(${plane.leftTop!!.blockX},${plane.leftTop!!.blockY},${plane.leftTop!!.blockZ})")
            "bottom" -> SBUtil.itemFactory(Material.MAGENTA_CONCRETE, "${ChatColor.LIGHT_PURPLE}LeftBottom", "${ChatColor.DARK_GRAY}(${plane.leftBottom!!.blockX},${plane.leftBottom!!.blockY},${plane.leftBottom!!.blockZ})")
            "left" -> SBUtil.itemFactory(Material.LIME_CONCRETE, "${ChatColor.GREEN}LeftTop", "${ChatColor.DARK_GRAY}(${plane.leftTop!!.blockX},${plane.leftTop!!.blockY},${plane.leftTop!!.blockZ})")
            "right" -> SBUtil.itemFactory(Material.ORANGE_CONCRETE, "${ChatColor.GOLD}RightTop", "${ChatColor.DARK_GRAY}(${plane.rightTop!!.blockX},${plane.rightTop!!.blockY},${plane.rightTop!!.blockZ})")
            else -> SBUtil.itemFactory(Material.BARRIER," ",null)
        }
        val to = when(side) {
            "top" -> SBUtil.itemFactory(Material.ORANGE_CONCRETE, "${ChatColor.GOLD}RightTop", "${ChatColor.DARK_GRAY}(${plane.rightTop!!.blockX},${plane.rightTop!!.blockY},${plane.rightTop!!.blockZ})")
            "bottom" -> SBUtil.itemFactory(Material.CYAN_CONCRETE, "${ChatColor.AQUA}RightBottom", "${ChatColor.DARK_GRAY}(${plane.rightBottom!!.blockX},${plane.rightBottom!!.blockY},${plane.rightBottom!!.blockZ})")
            "left" -> SBUtil.itemFactory(Material.MAGENTA_CONCRETE, "${ChatColor.LIGHT_PURPLE}LeftBottom", "${ChatColor.DARK_GRAY}(${plane.leftBottom!!.blockX},${plane.leftBottom!!.blockY},${plane.leftBottom!!.blockZ})")
            "right" -> SBUtil.itemFactory(Material.CYAN_CONCRETE, "${ChatColor.AQUA}RightBottom", "${ChatColor.DARK_GRAY}(${plane.rightBottom!!.blockX},${plane.rightBottom!!.blockY},${plane.rightBottom!!.blockZ})")
            else -> SBUtil.itemFactory(Material.BARRIER," ",null)
        }
        val controlPoints = plane.getControlPoints(side)
        if(!deletePoint){
            val deleteItem = SBUtil.itemFactory(Material.BARRIER, "${ChatColor.RED}Remove Control Point", null)
            val totalSizeNeeded = controlPoints.size*2 + 1 + 2 + 1 //*2+1 because every active control point should get 1 in front, and also +1 at the end
            //+2 because the from and the to items, +1 because of the remove control Points
            inventory = Bukkit.createInventory(null,((totalSizeNeeded-1)/9 + 1)*9, "$side control points editor" )
            this.addLockedItem(0, from)
            this.addLockedItem(controlPoints.size*2 + 1 + 1, to)
            this.addLockedItem(controlPoints.size*2 + 1 + 2, deleteItem){player ->
                ControlPointMenu(owner,location, side, true).open(player)
            }
            for(i in controlPoints.indices){
                val point = controlPoints[i]
                val newPoint = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.WHITE}Create New ControlPoint", "${ChatColor.DARK_GRAY}your location: (${location.blockX},${location.blockY},${location.blockZ})")
                val editPoint = SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}Change ControlPoint $i", listOf("${ChatColor.DARK_GRAY}Click to change location","${ChatColor.DARK_GRAY}currently: (${point.blockX},${point.blockY},${point.blockZ})"))
                this.addLockedItem(i*2 + 1, newPoint){player ->
                    plane.addControlPoints(side, i, location.blockLocation())
                    this.closeViewers()
                }
                this.addLockedItem(i*2 + 2, editPoint){player ->
                    plane.setControlPoint(side, i , location.blockLocation())
                    this.closeViewers()
                }
            }
            val newPoint = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.WHITE}Create New ControlPoint", "${ChatColor.DARK_GRAY}your location: (${location.blockX},${location.blockY},${location.blockZ})")
            this.addLockedItem(controlPoints.size*2 + 1, newPoint){player ->
                plane.addControlPoints(side,controlPoints.size, location.blockLocation())
                this.closeViewers()
            }
        }
        else{
            val totalSizeNeeded = controlPoints.size + 2
            inventory = Bukkit.createInventory(null,((totalSizeNeeded-1)/9 + 1)*9, "$side control points remover" )
            this.addLockedItem(0, from)
            this.addLockedItem(controlPoints.size + 1, to)
            for(i in controlPoints.indices){
                val point = controlPoints[i]
                val controlItem = SBUtil.itemFactory(Material.YELLOW_STAINED_GLASS, "${ChatColor.YELLOW}ControlPoint $i", listOf("${ChatColor.DARK_GRAY}Click to remove","${ChatColor.GRAY}(${point.blockX},${point.blockY},${point.blockZ})"))
                this.addLockedItem(i+1,controlItem ){ player ->
                    plane.removeControlPoint(side,i)
                    this.closeViewers()
                }
            }
        }

    }

    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        InventoryMenuSystem.removeInventory(this)
    }
}