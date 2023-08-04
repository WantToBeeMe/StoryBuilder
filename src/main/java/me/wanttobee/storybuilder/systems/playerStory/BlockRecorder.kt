package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandIntLeaf
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

class BlockRecorder {
    private val plugin = SBPlugin.instance
    private val undoStack : Array<MutableMap<Location, BlockData>?> = arrayOfNulls(10) //when te stack is full and you want to add something,
    private val redoStack : Array<MutableMap<Location, BlockData>?> = arrayOfNulls(10) //the bottom one gets deleted and everything gets moved down by 1 (not really a stack then anymore lol)
    
    private var currentAction : MutableMap<Location, BlockData>? = null

    fun runRecorderAsync(task : (BlockRecorder) -> Unit) {
        BlockRecorderSystem.enqueue {
            this.start()
            task.invoke(this)
            this.finish()
        }
    }
    fun runRecorderSynced(task : (BlockRecorder) -> Unit){
        this.start()
        task.invoke(this)
        this.finish()
    }

    private fun pushUndo(){
        if(currentAction == null) return
        val listSize =undoStack.size-1
        for(index in 0 until listSize)
            undoStack[ listSize-index ] = undoStack[listSize-index-1 ]
        undoStack[0] = currentAction
        currentAction = null
    }
    private fun pushRedo(){
        if(currentAction == null) return
        val listSize =redoStack.size-1
        for(index in 0 until listSize)
            redoStack[ listSize-index ] = redoStack[listSize-index-1 ]
        redoStack[0] = currentAction
        currentAction = null
    }

    private fun popUndo() : Boolean{
        if(undoStack[0] == null)
            return false
        for((loc, block) in undoStack[0]!!)
            place(loc, block)
        for(index in 0 until undoStack.size-1)
            undoStack[index] = undoStack[index+1]
        return true
    }
    private fun popRedo() : Boolean{
        if(redoStack[0] == null)
            return false

        for((loc, block) in redoStack[0]!!)
            place(loc, block)
        for(index in 0 until redoStack.size-1)
            redoStack[index] = redoStack[index+1]
        return true
    }

    fun undo(amount : Int, messenger : Player){
        runRecorderAsync{ _ ->
            var completed = 0
            val cappedAmount = Math.min(Math.max(1, amount), 10)
            if (undoStack.all { i -> i == null }) {
                messenger.sendMessage("${SBPlugin.title}${ChatColor.RED}there are no available edits to undo")
            } else {
                start()
                for (i in 0 until cappedAmount) {
                    if (!popUndo())
                        completed++
                }
                pushRedo()
                messenger.sendMessage("${SBPlugin.title}${ChatColor.GREEN}undid $completed available edits")
            }
        }

    }
    fun redo(amount : Int, messenger : Player){
        runRecorderAsync{ _ ->
            var completed = 0
            val cappedAmount = Math.min(Math.max(1, amount), 10)
            if(redoStack.all { i -> i==null }){
                messenger.sendMessage("${SBPlugin.title}${ChatColor.RED}there are no available edits to redo")
            } else{
                start()
                for(i in 0 until cappedAmount){
                    if(!popRedo())
                        completed++
                }
                pushUndo()
                messenger.sendMessage("${SBPlugin.title}${ChatColor.GREEN}redid $completed available edits")
            }
        }
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
                    {p, i -> StorySystem.getPlayersStory(p).undo(i) },
                    {p -> StorySystem.getPlayersStory(p).undo(1) })
        }

        val redo = Redo
        object Redo : ISystemCommand {
            override val exampleCommand: String= "/sd redo [Int/amount]"
            override val helpText: String = "to redo your last undone action"
            override val baseTree = CommandIntLeaf("redo", 1, 10,
                    {p, i -> StorySystem.getPlayersStory(p).redo(i) },
                    {p -> StorySystem.getPlayersStory(p).redo(1) })
        }
    }
}