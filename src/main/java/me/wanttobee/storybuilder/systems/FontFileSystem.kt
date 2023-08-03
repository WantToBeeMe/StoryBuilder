package me.wanttobee.storybuilder.systems
import me.wanttobee.storybuilder.SBPlugin
import java.io.File
import java.io.FileOutputStream
import java.awt.Font


object FontFileSystem {
    private val plugin = SBPlugin.instance
    private val folder = File(plugin.dataFolder, File.separator + "FontFiles")

    //private fun getResourceFont(name: String): Font {
    //    val inputStream = this.javaClass.getResourceAsStream("/$name")
    //    if (inputStream == null)
    //        plugin.logger.warning("'$name' not found in the resources folder.")
    //    val tempFile = File.createTempFile("font", ".ttf")
    //    FileOutputStream(tempFile).use { output ->
    //        inputStream.copyTo(output)
    //    }
    //    val font = Font.createFont(Font.TRUETYPE_FONT, tempFile)
    //    tempFile.delete()
    //    return font
    //}

    fun initialize(){
        folder.mkdirs()
        val resourceFonts = arrayOf("Lumanosimo-Regular.ttf", "Anton-Regular.ttf" ,"Lobster-Regular.ttf", "Borel-Regular.ttf")
        for(resourceFontPath in resourceFonts){
            val inputStream = this.javaClass.getResourceAsStream("/defaultFonts/$resourceFontPath")
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

    fun getFont(name: String): Font? {
        val fileName = if(name.endsWith(".ttf")) name else "$name.ttf"
        val file = File(folder, File.separator + fileName)
        if (!file.exists()) return null
        return  Font.createFont(Font.TRUETYPE_FONT, file)
    }

}