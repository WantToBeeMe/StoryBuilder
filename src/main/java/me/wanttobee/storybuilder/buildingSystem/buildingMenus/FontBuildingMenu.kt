package me.wanttobee.storybuilder.buildingSystem.buildingMenus

import me.wanttobee.storybuilder.SBUtil
import me.wanttobee.storybuilder.buildingSystem.RatioMode
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
        initAlignmentButton(story)
        initFillButton(story)
        initRatioPreferenceButtons(story)
        initOutOfBounds(story)
        initFontSize(story)
    }

    private fun initSeparators(){
        this.addLockedItem(0,7, separator)
        for(i in 0 until 9)
            this.addLockedItem(1,i, separator)
    }

    private fun initEmptyButtons(){
        val emptyButton = SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.GRAY}Empty", null)
        this.addLockedItem(2,3,emptyButton)
        this.addLockedItem(2,4,emptyButton)
        this.addLockedItem(2,5,emptyButton)
        this.addLockedItem(2,6,emptyButton)
        this.addLockedItem(2,7,emptyButton)
        this.addLockedItem(2,8,emptyButton)
    }

    private fun initAlignmentButton(story : PlayersStory){
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
        val alignmentButton = SBUtil.itemFactory(Material.YELLOW_CONCRETE_POWDER, "${ChatColor.WHITE}Alignment: ${ChatColor.YELLOW}$alignmentButtonText", "${ChatColor.GRAY}Click to change")
        this.addLockedItem(2,0,alignmentButton) {player ->
            AlignmentPickerMenu(player){openThisWindowAgain(player)}.open(player)
        }
    }

    private fun initFillButton(story : PlayersStory){
        val fillButton = if(story.fontFill) SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}Fill: ${ChatColor.GREEN}Enabled", listOf("${ChatColor.GRAY}Click to disable","${ChatColor.DARK_GRAY}Fills the characters"))
        else SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}Fill: ${ChatColor.GRAY}Disabled", listOf("${ChatColor.GRAY}Click to enable","${ChatColor.DARK_GRAY}Fills the characters"))

        this.addLockedItem(2,2, fillButton){player ->
            StorySystem.getPlayersStory(player).fontFill = !StorySystem.getPlayersStory(player).fontFill
            openThisWindowAgain(player)
        }
    }

    private fun initRatioPreferenceButtons(story : PlayersStory){
        val ratioButton = when(story.fontRatio){
            RatioMode.LEFT_TOP -> {SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}RatioMode: ${ChatColor.GREEN}Left to Top", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY} 1/5")}
            RatioMode.RIGHT_TOP -> {SBUtil.itemFactory(Material.ORANGE_CONCRETE_POWDER, "${ChatColor.WHITE}RatioMode: ${ChatColor.GOLD}Right to Top", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY} 2/5")}
            RatioMode.LEFT_BOTTOM -> {SBUtil.itemFactory(Material.MAGENTA_CONCRETE_POWDER, "${ChatColor.WHITE}RatioMode: ${ChatColor.LIGHT_PURPLE}Left to Bottom", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY} 3/5")}
            RatioMode.RIGHT_BOTTOM -> {SBUtil.itemFactory(Material.CYAN_CONCRETE_POWDER, "${ChatColor.WHITE}RatioMode: ${ChatColor.AQUA}Right to Top", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY} 4/5")}
            RatioMode.NONE -> {SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}RatioMode: ${ChatColor.GRAY}None ${ChatColor.DARK_GRAY}(so stretch to fill)", "${ChatColor.GRAY}Click to change ${ChatColor.DARK_GRAY} 5/5")}
        }

        this.addLockedItem(2, 1, ratioButton){player ->
            StorySystem.getPlayersStory(player).fontRatio = when(story.fontRatio){
                RatioMode.LEFT_TOP -> {RatioMode.RIGHT_TOP}
                RatioMode.RIGHT_TOP -> {RatioMode.LEFT_BOTTOM}
                RatioMode.LEFT_BOTTOM -> {RatioMode.RIGHT_BOTTOM}
                RatioMode.RIGHT_BOTTOM -> {RatioMode.NONE}
                RatioMode.NONE -> {RatioMode.LEFT_TOP}
            }
            openThisWindowAgain(player)
        }
    }

    private fun initOutOfBounds(story : PlayersStory){
        val outOfBoundButton = if(story.fontOutOfBound) SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}OutOfBound: ${ChatColor.GREEN}Enabled", listOf("${ChatColor.GRAY}Click to disable","${ChatColor.DARK_GRAY}Lets fonts build outside the paper",if(!story.useFontSize)"${ChatColor.DARK_GRAY}Doesnt effect when FontSize is set to Auto" else if(story.fontRatio == RatioMode.NONE)"${ChatColor.DARK_GRAY}Doesnt effect when ratioMode is None" else ""))
        else SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}OutOfBound: ${ChatColor.GRAY}Disabled", listOf("${ChatColor.GRAY}Click to enable","${ChatColor.DARK_GRAY}Lets fonts build outside the paper"))

        this.addLockedItem(2,3, outOfBoundButton){player ->
            StorySystem.getPlayersStory(player).fontOutOfBound = !StorySystem.getPlayersStory(player).fontOutOfBound
            openThisWindowAgain(player)
        }
    }

    private fun initFontSize(story : PlayersStory){
        val fontSize = if(!story.useFontSize) SBUtil.itemFactory(Material.LIME_CONCRETE_POWDER, "${ChatColor.WHITE}FontSize: ${ChatColor.GREEN}Auto", listOf("${ChatColor.GRAY}Click to set to Set","${ChatColor.DARK_GRAY}auto calculated the size of the font","${ChatColor.DARK_GRAY}using the ratio preference"))
        else SBUtil.itemFactory(Material.GRAY_CONCRETE_POWDER, "${ChatColor.WHITE}FontSize: ${ChatColor.GRAY}Set ${ChatColor.DARK_GRAY}(currently ${story.fontSize})", listOf("${ChatColor.GRAY}Click to set to Auto","${ChatColor.DARK_GRAY}change the size by:","${ChatColor.DARK_GRAY}/sd fontSize [amount/Int]"))

        this.addLockedItem(2,4, fontSize){ player ->
            StorySystem.getPlayersStory(player).useFontSize = !StorySystem.getPlayersStory(player).useFontSize
            openThisWindowAgain(player)
        }
    }


}