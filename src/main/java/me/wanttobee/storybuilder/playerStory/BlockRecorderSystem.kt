package me.wanttobee.storybuilder.playerStory

import me.wanttobee.storybuilder.SBPlugin

object BlockRecorderSystem {
    private val plugin = SBPlugin.instance
    private val queue: MutableList<() -> Unit> = mutableListOf()

    private val queueLock = Object()

    fun enqueue(block: () -> Unit) {
        synchronized(queueLock) {
            queue.add(block)
        }
    }

    private fun dequeue(): (() -> Unit)? {
        synchronized(queueLock) {
            return if (queue.isNotEmpty()) {
                queue.removeAt(0)
            } else {
                null
            }
        }
    }

    fun startBlockRecorder(){
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin,{
            val task = dequeue()
            if(task != null) task.invoke()
        },0,1 )
    }
}