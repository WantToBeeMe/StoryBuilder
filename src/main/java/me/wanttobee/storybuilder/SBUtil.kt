package me.wanttobee.storybuilder

import org.bukkit.Material
import org.bukkit.NamespacedKey
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

}
