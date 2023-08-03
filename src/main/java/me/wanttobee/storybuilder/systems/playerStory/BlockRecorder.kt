package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandIntLeaf
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.data.BlockData

class BlockRecorder {
    private val undoList : Array<MutableMap<Location, BlockData>?> = arrayOfNulls(10)
    private val redoList : Array<MutableMap<Location, BlockData>?> = arrayOfNulls(10)
    
    private var currentAction : MutableMap<Location, BlockData>? = null

    private fun pushUndo(){
        if(currentAction == null) return
        val listSize =undoList.size-1
        for(index in 0 until listSize)
            undoList[ listSize-index ] = undoList[listSize-index-1 ]
        undoList[0] = currentAction
        currentAction = null
    }
    private fun pushRedo(){
        if(currentAction == null) return
        val listSize =redoList.size-1
        for(index in 0 until listSize)
            redoList[ listSize-index ] = redoList[listSize-index-1 ]
        redoList[0] = currentAction
        currentAction = null
    }

    private fun popUndo() : Boolean{
        if(undoList[0] == null)
            return false
        for((loc, block) in undoList[0]!!)
            place(loc, block)
        for(index in 0 until undoList.size-1)
            undoList[index] = undoList[index+1]
        return true
    }
    private fun popRedo() : Boolean{
        if(redoList[0] == null)
            return false

        for((loc, block) in redoList[0]!!)
            place(loc, block)
        for(index in 0 until redoList.size-1)
            redoList[index] = redoList[index+1]
        return true
    }

    fun undo(amount : Int) : Boolean{
        var completed = true
        val cappedAmount = Math.min(Math.max(1, amount), 10)
        if(undoList.all { i -> i==null }) return false
        start()
        for(i in 0 until cappedAmount){
            if(!popUndo())
                completed = false
        }
        pushRedo()
        return completed
    }
    fun redo(amount : Int) : Boolean{
        var completed = true
        val cappedAmount = Math.min(Math.max(1, amount), 10)
        if(redoList.all { i -> i==null }) return false
        start()
        for(i in 0 until cappedAmount){
            if(!popRedo())
                completed = false
        }
        pushUndo()
        return completed
    }

    fun place(location : Location, blockData : BlockData ){
        val locationWorld = location.world ?: return
        val blockLock = location.blockLocation()
        if(currentAction != null){
            if(!currentAction!!.containsKey(blockLock))
                currentAction!![blockLock] = location.block.blockData
        }
        locationWorld.setBlockData(blockLock,blockData )
    }


    fun start() : BlockRecorder{
        currentAction = mutableMapOf()
        return this
    }
    fun finish(){ pushUndo() }
//for(i in undoList.indices){
    //    SBPlugin.instance.logger.info(i.toString() + " - " +(undoList[i] != null).toString())
    //}

    companion object{
        val undo = Undo
        object Undo : ISystemCommand {
            override val exampleCommand: String= "/sd undo [Int/amount]"
            override val helpText: String = "to undo your last action"
            override val baseTree = CommandIntLeaf("undo", 1, 10,
                    {p, i -> val succeeded = StorySystem.getPlayersStory(p).undo(i)
                        if(succeeded) p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}undo done")
                        else p.sendMessage("${SBPlugin.title}${ChatColor.RED}no more undoes left")},
                    {p -> val succeeded = StorySystem.getPlayersStory(p).undo(1)
                        if(succeeded) p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}undo done")
                        else p.sendMessage("${SBPlugin.title}${ChatColor.RED}no more undoes left")
                    })
        }

        val redo = Redo
        object Redo : ISystemCommand {
            override val exampleCommand: String= "/sd redo [Int/amount]"
            override val helpText: String = "to redo your last undone action"
            override val baseTree = CommandIntLeaf("redo", 1, 10,
                    {p, i -> val succeeded = StorySystem.getPlayersStory(p).redo(i)
                        if(succeeded) p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}redo done")
                        else p.sendMessage("${SBPlugin.title}${ChatColor.RED}no more redoes left")},
                    {p -> val succeeded = StorySystem.getPlayersStory(p).redo(1)
                        if(succeeded) p.sendMessage("${SBPlugin.title}${ChatColor.GREEN}redo done")
                        else p.sendMessage("${SBPlugin.title}${ChatColor.RED}no more redoes left")})
        }
    }
}