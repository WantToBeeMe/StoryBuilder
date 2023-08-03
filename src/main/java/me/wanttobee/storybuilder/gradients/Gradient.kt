package me.wanttobee.storybuilder.gradients

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Gradient(val name : String, private var blocks : Array<Material>) {
    val size = blocks.size
    init{
        if(blocks.isEmpty())
            blocks = arrayOf(Material.STONE)
    }

    fun getReal(index : Int) : Material?{
        if(index >= blocks.size) return null
        return blocks[index]
    }

    fun get(index : Int) : Material{
        if(index >= blocks.size)
            return blocks.last()
        else return blocks[index]
    }
    fun get(percentage : Double) : Material{
        return blocks[((blocks.size-1) * percentage).toInt()]
    }

    override fun toString(): String { return name }

    fun giveToPlayer(player: Player){
        for (material in blocks) {
            val item = ItemStack(material)
            player.inventory.addItem(item)
        }
    }
}