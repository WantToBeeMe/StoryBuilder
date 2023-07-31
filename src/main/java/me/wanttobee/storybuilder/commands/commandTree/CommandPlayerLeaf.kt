package me.wanttobee.storybuilder.commands.commandTree

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandPlayerLeaf private constructor(arg : String,private val realTimePossiblePlayers : (() -> Collection<Player>)?, private val possiblePlayers: Collection<Player>?, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Player>(arg,effect, emptyEffect) {
    constructor(arg : String, possiblePlayers: Collection<Player>, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,null, possiblePlayers,effect,emptyEffect)
    constructor(arg: String, realTimePossiblePlayers : () -> Collection<Player>, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(arg,realTimePossiblePlayers,null,effect, emptyEffect)

    override fun validateValue(sender: Player, tailArgs: Array<String>): Player? {
        if(tailArgs.isEmpty()) return null
        //var isRealPlayer = false
        //for(player in MASEGPlugin.instance.server.onlinePlayers){
        //    if(player.name.lowercase() == tailArgs.first()){
        //        isRealPlayer = true
        //        break
        //    }
        //}
        //if(!isRealPlayer){
        //    sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}is not online")
        //    return null
        //}
        if(realTimePossiblePlayers != null){
            for(pos in realTimePossiblePlayers.invoke()){
                if(pos.name.lowercase() == tailArgs.first())
                    return pos
            }
        }
        if(possiblePlayers != null){
            for(pos in possiblePlayers){
                if(pos.name.lowercase() == tailArgs.first())
                    return pos
            }
        }
        sender.sendMessage("${ChatColor.GRAY}${tailArgs.first()} ${ChatColor.RED}cant be entered as an argument")
        return null
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if((possiblePlayers != null && possiblePlayers.isEmpty()) || (realTimePossiblePlayers != null && realTimePossiblePlayers.invoke().isEmpty()) ){
            if("" == currentlyTyping)
                list.add("no players available")
        } else {
            if(possiblePlayers != null){
                for (pos in possiblePlayers) {
                    if (pos.name.lowercase().contains(currentlyTyping))
                        list.add(pos.name)
                }
            }
            if(realTimePossiblePlayers != null){
                for (pos in realTimePossiblePlayers.invoke()) {
                    if (pos.name.lowercase().contains(currentlyTyping))
                        list.add(pos.name)
                }
            }
        }
        return list
    }
}