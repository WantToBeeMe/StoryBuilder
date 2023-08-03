package me.wanttobee.storybuilder

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object SBUtil {

    val IDKey: NamespacedKey = NamespacedKey(SBPlugin.instance, "maseg_identifier")
    private var id = 0
    fun itemFactory(material: Material, title: String, lore: List<String>?,amount: Int, enchanted : Boolean = false): ItemStack {
        val itemStack = ItemStack(material, amount)
        val itemMeta = itemStack.itemMeta
        itemMeta?.setDisplayName(title)
        itemMeta?.persistentDataContainer?.set(IDKey, PersistentDataType.INTEGER, id++)
        itemMeta?.lore = lore
        itemStack.itemMeta = itemMeta
        if (enchanted) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            val meta = itemStack.itemMeta
            meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            itemStack.itemMeta = meta
        }
        return itemStack
    }
    fun itemFactory(material: Material, title: String, lore: String, enchanted : Boolean = false): ItemStack {
        return itemFactory(material, title, listOf(lore), 1,enchanted)
    }
    fun itemFactory(material: Material, title: String,  lore: List<String>?, enchanted : Boolean = false): ItemStack {
        return itemFactory(material, title, lore, 1,enchanted)
    }
    fun itemFactory(material: Material, title: String, lore: String, amount:Int, enchanted : Boolean = false): ItemStack {
        return itemFactory(material, title, listOf(lore), amount,enchanted)
    }

    fun Location.blockLocation(): Location {
        return Location(world, blockX.toDouble(), blockY.toDouble(), blockZ.toDouble())
    }
    fun Location.rigidParticles(particle : Particle, dustOptions : Particle.DustOptions? ) {
        if(this.world == null) return
        val particles = 3
        for(i in 0..particles){
            world!!.spawnParticle(particle, x+(i/particles.toDouble()),y+1,z+1,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+(i/particles.toDouble()),y+1,z+0,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+(i/particles.toDouble()),y+0,z+1,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+(i/particles.toDouble()),y+0,z+0,1, 0.0,0.0,0.0, dustOptions)

            world!!.spawnParticle(particle, x+1,y+(i/particles.toDouble()),z+1,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+1,y+(i/particles.toDouble()),z+0,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+0,y+(i/particles.toDouble()),z+1,1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+0,y+(i/particles.toDouble()),z+0,1, 0.0,0.0,0.0, dustOptions)

            world!!.spawnParticle(particle, x+1,y+1,z+(i/particles.toDouble()),1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+0,y+1,z+(i/particles.toDouble()),1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+1,y+0,z+(i/particles.toDouble()),1, 0.0,0.0,0.0, dustOptions)
            world!!.spawnParticle(particle, x+0,y+0,z+(i/particles.toDouble()),1, 0.0,0.0,0.0, dustOptions)
        }
    }

    fun getRealName(material: Material): String {
        val name = material.name.lowercase()
        var words = name.split("_")

        if (words.size == 2 && words[1] == "minecart") {
            words = listOf(words[1], "with", words[0])
        }
        else if(words.contains("template")) return "Smithing Template"
        else if(words.contains("music")) return "Music Disc"

        val formattedWords = words.map { word ->
            when (word) {
                "of", "on", "a", "with" -> word
                "tnt" -> word.uppercase()
                else -> word.capitalize()
            }
        }
        return formattedWords.joinToString(" ")
    }
}
