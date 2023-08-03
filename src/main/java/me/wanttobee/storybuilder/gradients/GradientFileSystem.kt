package me.wanttobee.storybuilder.gradients

import me.wanttobee.storybuilder.SBPlugin
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileOutputStream

object GradientFileSystem {
    private val plugin = SBPlugin.instance
    private val folder = File(plugin.dataFolder, File.separator + "GradientFiles")

    //private fun getResourceGradient(name: String): Gradient {
    //    val inputStream = this.javaClass.getResourceAsStream("/$name")
    //    if (inputStream == null)
    //        this.plugin.logger.warning("'$name' not found in the resources folder.")
    //    val tempFile = File.createTempFile("tempFile", ".yml")
    //    FileOutputStream(tempFile).use { output ->
    //        inputStream.copyTo(output)
    //    }
    //    val ymlConfig = YamlConfiguration.loadConfiguration(tempFile)
    //    val gradient = fileToGradient(name.removeSuffix(".yml"), ymlConfig)!!
    //    tempFile.delete()
    //    return gradient
    //}

    fun initialize(){
        this.folder.mkdirs()

       val resourceGradients = arrayOf("paper.yml","magic.yml","lava.yml","darkRed.yml")
       for(resourceGradientPath in resourceGradients){
           val inputStream = this.javaClass.getResourceAsStream("/defaultGradients/$resourceGradientPath")
           if (inputStream == null)
               this.plugin.logger.warning("'$resourceGradientPath' not found in the resources folder.")
           else{
               val newFontFile = File(this.folder, File.separator + resourceGradientPath)
               FileOutputStream(newFontFile).use { output -> inputStream.copyTo(output) }
           }
       }
    }

    fun getAllFiles(withExtension : Boolean = true): Array<String> {
        if (!this.folder.exists() || !this.folder.isDirectory) return emptyArray()
        val folderList =  this.folder.list { _, name -> name.endsWith(".yml") } ?: emptyArray()
        return if(withExtension) folderList
        else Array(folderList.size) { i -> folderList[i].removeSuffix(".yml")}
    }

    fun getGradient(name: String): Gradient? {
        val fileName = if(name.endsWith(".yml")) name else "$name.yml"

        val file = File(this.folder, File.separator + fileName)
        if (!file.exists()) return null
        val ymlConfig = YamlConfiguration.loadConfiguration(file)
        return fileToGradient(name.removeSuffix(".yml"),ymlConfig)
    }

    private fun fileToGradient(name : String, file : FileConfiguration) : Gradient{
        val itemList = mutableListOf<Material>()
        for(i in 1..(9*4)){
            val value = file.getString("G$i") ?: break
            val material = Material.getMaterial(value) ?: return Gradient(name, arrayOf(Material.BARRIER))
            if(!material.isBlock) return Gradient(name, arrayOf(Material.BARRIER))
            itemList.add( material )
        }
        return Gradient(name, itemList.toTypedArray())
    }


    fun saveGradient(name: String, materials : Array<Material>) : Boolean{
        if(materials.isEmpty()) return false
        for(mat in materials)
            if(!mat.isBlock) return false

        val fileName = if(name.endsWith(".yml")) name else "$name.yml"
        val file = File(this.folder, File.separator + fileName)
        val ymlConfig = YamlConfiguration.loadConfiguration(file)
        for(i in materials.indices){
            ymlConfig.createSection("G${i+1}")
            ymlConfig.set("G${i+1}",materials[i].toString())
        }
        ymlConfig.save(file)
        return true
    }

}