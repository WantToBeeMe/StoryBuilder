package me.wanttobee.storybuilder.buildingSystem.buildingMenus

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.buildingSystem.ClampMode
import me.wanttobee.storybuilder.buildingSystem.ClampSides
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.playerStory.PlayersStory
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class FontBuildingMenu(owner : Player, doneEffect : ()->Unit)  : IBuildMenu( owner,doneEffect) {
    override var inventory: Inventory = Bukkit.createInventory(null, 9*3, "Font building menu")

    override fun openThisWindowAgain(player: Player) { FontBuildingMenu(owner,doneEffect).open(player) }

    override fun reloadGradient() { this.loadGradient(0,0,6) }

    init{
        InventoryMenuSystem.addInventory(this)
        this.reloadGradient()
        setDoneButton(0,8)
        initSeparators()

        val story = StorySystem.getPlayersStory(owner)
        initEmptyButtons()
        initAlignmentButton(  story,2,0)
        initClampSideButtons( story,2,1)
        initClampModeButtons( story,2,2)
        initOutOfBoundsButton(story,2,3)
        initFontSizeButton(   story,2,5)
        initFillButton(       story,2,6)
        initFontBoundBoxButton(story,2,4)
    }

    private fun initSeparators(){
        this.addLockedItem(0,7, separator)
        for(i in 0 until 9)
            this.addLockedItem(1,i, separator)
    }

    private fun initEmptyButtons(){
        val emptyButton = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.GRAY}Empty", null)
        this.addLockedItem(2,6,emptyButton)
        this.addLockedItem(2,7,emptyButton)
        this.addLockedItem(2,8,emptyButton)
    }

    private fun initAlignmentButton(story : PlayersStory, row: Int, column : Int){
        val alignmentButtonText = when(story.alignment){
            Alignment.LEFT_TOP -> "Left Top"
            Alignment.CENTERED_TOP ->  "Top Centered"
            Alignment.RIGHT_TOP -> "Right Top"
            Alignment.CENTERED_LEFT -> "Left Centered"
            Alignment.CENTERED -> "Centered"
            Alignment.CENTERED_RIGHT -> "Right Centered"
            Alignment.LEFT_BOTTOM -> "Left Bottom"
            Alignment.CENTERED_BOTTOM  -> "Bottom Centered"
            Alignment.RIGHT_BOTTOM  -> "Right Bottom"
        }
        val alignmentButton = SBUtil.itemFactory(Material.ORANGE_CONCRETE_POWDER, "${ChatColor.WHITE}Alignment: ${ChatColor.GOLD}$alignmentButtonText", "${ChatColor.GRAY}Click to change")
        this.addLockedItem(row,column,alignmentButton) {player ->
            AlignmentPickerMenu(player){ openThisWindowAgain(player) }.open(player)
        }
    }

    private fun initFillButton(story : PlayersStory, row: Int, column : Int){
        val fillButton = if(story.fontFill) SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}Fill: ${ChatColor.GREEN}Enabled", listOf("${ChatColor.GRAY}Click to disable","${ChatColor.DARK_GRAY}Fills the characters"))
        else SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}Fill: ${ChatColor.GRAY}Disabled", listOf("${ChatColor.GRAY}Click to enable","${ChatColor.DARK_GRAY}Fills the characters"))

        this.addLockedItem(row,column, fillButton){player ->
            StorySystem.getPlayersStory(player).fontFill = !StorySystem.getPlayersStory(player).fontFill
            openThisWindowAgain(player)
        }
    }

    private fun initOutOfBoundsButton(story : PlayersStory, row: Int, column : Int){
        val outOfBoundButton = if(story.fontOutOfBound) SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}OutOfBound: ${ChatColor.GREEN}Enabled", listOf("${ChatColor.GRAY}Click to disable","${ChatColor.DARK_GRAY}allows fonts to be build outside the paper"))
        else SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}OutOfBound: ${ChatColor.GRAY}Disabled", listOf("${ChatColor.GRAY}Click to enable","${ChatColor.DARK_GRAY}Lets fonts build outside the paper"))

        this.addLockedItem(row,column, outOfBoundButton){player ->
            StorySystem.getPlayersStory(player).fontOutOfBound = !StorySystem.getPlayersStory(player).fontOutOfBound
            openThisWindowAgain(player)
        }
    }

    private fun initFontBoundBoxButton(story : PlayersStory, row: Int, column : Int){
        val fontBoundBox = if(story.fontLogicBoundingBox) SBUtil.itemFactory(Material.YELLOW_CONCRETE_POWDER, "${ChatColor.WHITE}BoundingBox type: ${ChatColor.YELLOW}Logic", listOf("${ChatColor.GRAY}Click to set to Visual","${ChatColor.DARK_GRAY}logic boundingBox have the same height,","${ChatColor.DARK_GRAY}no matter the character"))
        else SBUtil.itemFactory(Material.CYAN_CONCRETE_POWDER, "${ChatColor.WHITE}BoundingBox type: ${ChatColor.AQUA}Visual", listOf("${ChatColor.GRAY}Click to set to Logic","${ChatColor.DARK_GRAY}visual boundingBox, all characters have","${ChatColor.DARK_GRAY}a boundingBox perfectly fitting there symbol"))

        this.addLockedItem(row,column, fontBoundBox){ player ->
            StorySystem.getPlayersStory(player).fontLogicBoundingBox = !StorySystem.getPlayersStory(player).fontLogicBoundingBox
            openThisWindowAgain(player)
        }
    }


    private fun initClampSideButtons(story : PlayersStory, row: Int, column : Int){
        val clampButtonText = when(story.fontClampSide){
            ClampSides.LEFT_OR_TOP ->    Pair("Left and Top","1/4")
            ClampSides.LEFT_OR_BOTTOM ->  Pair("Left and Bottom","2/4")
            ClampSides.RIGHT_OR_TOP  ->   Pair("Right and Top","3/4")
            ClampSides.RIGHT_OR_BOTTOM  ->  Pair("Right and Bottom","4/4")
        }
        val alignmentButton = SBUtil.itemFactory(Material.ORANGE_CONCRETE_POWDER, "${ChatColor.WHITE}ClampSides: ${ChatColor.GOLD}${clampButtonText.first}", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY}${clampButtonText.second}")

        this.addLockedItem(row, column, alignmentButton){player ->
            story.fontClampSide = when(story.fontClampSide){
                ClampSides.LEFT_OR_TOP ->ClampSides.LEFT_OR_BOTTOM
                ClampSides.LEFT_OR_BOTTOM ->ClampSides.RIGHT_OR_TOP
                ClampSides.RIGHT_OR_TOP  ->ClampSides.RIGHT_OR_BOTTOM
                ClampSides.RIGHT_OR_BOTTOM  ->ClampSides.LEFT_OR_TOP
            }
            openThisWindowAgain(player)
        }
    }

    private fun initClampModeButtons(story : PlayersStory, row: Int, column : Int){
        val clampButtonText = when(story.fontClampMode){
            ClampMode.AUTO ->    Pair("Auto","1/4")
            ClampMode.HEIGHT ->  Pair("Height","2/4")
            ClampMode.WIDTH ->   Pair("Width","3/4")
            ClampMode.NONE  ->   Pair("None","4/4")
        }
        val none = story.fontClampMode == ClampMode.NONE
        val alignmentButton = SBUtil.itemFactory(
                if(none) Material.GRAY_CONCRETE_POWDER else Material.ORANGE_CONCRETE_POWDER,
                "${ChatColor.WHITE}ClampMode: ${if(none)ChatColor.GRAY else ChatColor.GOLD}${clampButtonText.first}", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY}${clampButtonText.second}")

        this.addLockedItem(row, column, alignmentButton){player ->
            story.fontClampMode = when(story.fontClampMode){
                ClampMode.AUTO ->  ClampMode.HEIGHT
                ClampMode.HEIGHT ->  ClampMode.WIDTH
                ClampMode.WIDTH ->  ClampMode.NONE
                ClampMode.NONE  ->  ClampMode.AUTO
            }
            openThisWindowAgain(player)
        }
    }



    private fun initFontSizeButton(story : PlayersStory, row: Int, column : Int){
        val fontSize = if(!story.useFontSize) SBUtil.itemFactory(Material.YELLOW_CONCRETE_POWDER, "${ChatColor.WHITE}FontSize: ${ChatColor.YELLOW}Auto", listOf("${ChatColor.GRAY}Click to set to Set","${ChatColor.DARK_GRAY}auto calculated the size of the font","${ChatColor.DARK_GRAY}using the ratio preference"))
        else SBUtil.itemFactory(Material.CYAN_CONCRETE_POWDER, "${ChatColor.WHITE}FontSize: ${ChatColor.AQUA}Set ${ChatColor.DARK_GRAY}(currently ${story.fontSize})", listOf("${ChatColor.GRAY}Click to set to Auto","${ChatColor.DARK_GRAY}change the size by:","${ChatColor.DARK_GRAY}/sd fontSize [amount/Int]"))

        this.addLockedItem(row,column, fontSize){ player ->
            StorySystem.getPlayersStory(player).useFontSize = !StorySystem.getPlayersStory(player).useFontSize
            openThisWindowAgain(player)
        }
    }


}