package me.wanttobee.storybuilder.systems
import me.wanttobee.storybuilder.SBPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.File
import java.io.FileOutputStream
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator


object FontSystem {
    private val plugin = SBPlugin.instance
    val folder = File(plugin.dataFolder, File.separator + "FontFiles")
    private var currentFontPath = "Lumanosimo-Regular.ttf"
    var font : Font = getResourceFont(currentFontPath)
        private set

    private fun getResourceFont(name: String): Font {
        val inputStream = this.javaClass.getResourceAsStream("/$name")
        if (inputStream == null)
            plugin.logger.warning("'$name' not found in the resources folder.")
        val tempFile = File.createTempFile("font", ".ttf")
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        val font = Font.createFont(Font.TRUETYPE_FONT, tempFile)
        tempFile.delete()
        return font
    }

    fun initializeFonts(){
        folder.mkdirs()
        val resourceFonts = arrayOf("Lumanosimo-Regular.ttf", "Anton-Regular.ttf" ,"Lobster-Regular.ttf", "Borel-Regular.ttf")
        for(resourceFontPath in resourceFonts){
            val inputStream = this.javaClass.getResourceAsStream("/$resourceFontPath")
            if (inputStream == null)
                plugin.logger.warning("'$resourceFontPath' not found in the resources folder.")
            else{
                val newFontFile = File(folder, File.separator + resourceFontPath)
                FileOutputStream(newFontFile).use { output -> inputStream.copyTo(output) }
            }
        }
    }

    fun getAllFiles(withExtension : Boolean = true): Array<String> {
        if (!folder.exists() || !folder.isDirectory) return emptyArray()
        val folderList =  folder.list { _, name -> name.endsWith(".ttf") } ?: emptyArray()
        return if(withExtension) folderList
        else Array(folderList.size) { i -> folderList[i].removeSuffix(".ttf")}
    }

    private fun getFont(name: String): Font? {
        val fileName = if(name.endsWith(".ttf")) name else "$name.ttf"
        val file = File(folder, File.separator + fileName)
        if (!file.exists()) return null
        return  Font.createFont(Font.TRUETYPE_FONT, file)
    }

    fun loadFont(commander: Player, fontPath: String){
        val newFont = getFont(fontPath)
        if(newFont == null){
            commander.sendMessage("${ChatColor.RED}Cant find font file: ${ChatColor.GRAY}$fontPath")
            return
        }
        font = newFont
        currentFontPath = fontPath
        commander.sendMessage("${ChatColor.GREEN}font has been changed to:")
        currentFontMessage(commander)
    }
    fun currentFontMessage(commander:Player){
        commander.sendMessage("${ChatColor.WHITE}${currentFontPath.removeSuffix(".ttf")}> ${ChatColor.GOLD}${font.fontName}")
    }

}