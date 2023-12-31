package me.wanttobee.storybuilder;

import me.wanttobee.storybuilder.gradients.GradientFileSystem
import me.wanttobee.storybuilder.inventoryMenus.InventoryMenuSystem
import me.wanttobee.storybuilder.buildingSystem.font.FontFileSystem
import me.wanttobee.storybuilder.playerStory.BlockRecorderSystem
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.annotation.command.Command
import org.bukkit.plugin.java.annotation.command.Commands
import org.bukkit.plugin.java.annotation.dependency.Library
import org.bukkit.plugin.java.annotation.plugin.ApiVersion
import org.bukkit.plugin.java.annotation.plugin.Description
import org.bukkit.plugin.java.annotation.plugin.Plugin
import org.bukkit.plugin.java.annotation.plugin.author.Author


@Plugin(name = "StoryBuilder", version ="S-1.0")
@ApiVersion(ApiVersion.Target.v1_20)
@Author("WantToBeeMe")
@Description("building tools helping you to tell the perfect story")

@Commands(
    Command(name = "storyBuilder", aliases = ["sb","st"], usage = "/sb"),
)

@Library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22") //kotlin !!
class SBPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: SBPlugin
        val title = "${ChatColor.GRAY}[${ChatColor.GOLD}Story Builder${ChatColor.GRAY}]${ChatColor.RESET} "
    }

    override fun onEnable() {
        instance = this

        getCommand("storyBuilder")?.setExecutor(SBCommands)
        getCommand("storyBuilder")?.tabCompleter = SBCommands

        FontFileSystem.initialize()
        GradientFileSystem.initialize()

        BlockRecorderSystem.startBlockRecorder()
        server.pluginManager.registerEvents(InventoryMenuSystem, this)
        server.pluginManager.registerEvents(StorySystem, this)
        server.scheduler.scheduleSyncRepeatingTask(this, { StorySystem.everyTick() } , 0, 1 )

        StartUpTests.run()
    }

    override fun onDisable() {}
}
