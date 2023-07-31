package me.wanttobee.storybuilder.systems.textBox

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player

class TextBox(private val owner : Player) {

    var leftTop : Location
        private set
    var rightTop : Location
        private set
    var leftBot : Location
        private set
    var rightBot : Location
        private set

    private var editingPoint : String? = null
    private var editingDistance = 4

    init{
        this.leftTop = owner.location
        this.rightTop = owner.location.add(5.0, 0.0, 0.0)
        this.leftBot = owner.location.add(0.0, -5.0, 0.0)
        this.rightBot = owner.location.add(5.0, -5.0, 0.0)
    }
    val topControlPoints = mutableListOf<Location>() //this can be empty, or also hold multiple control points, it should all work fine
    val botControlPoints = mutableListOf<Location>()
    val leftControlPoints = mutableListOf<Location>()
    val rightControlPoints = mutableListOf<Location>()

    fun isEditing() : Boolean{
        return editingPoint != null
    }
    fun editCorner(corner: Location?){
        if(corner == null) {
            editingPoint = null
            return
        }
        editingDistance = owner.eyeLocation.distance(corner).toInt()
        editingPoint = if(corner == leftTop) "leftTop"
        else if(corner == rightTop) "rightTop"
        else if (corner == leftBot) "leftBot"
        else if(corner == rightBot) "rightBot"
        else null

    }
    fun everyTick(){
        if(leftTop.world != owner.world) return
        val cornerParticles = Particle.FLAME // Change this to the particle you want to summon

        owner.world.spawnParticle(cornerParticles, leftTop, 1, 0.0, 0.0, 0.0, 0.0)
        owner.world.spawnParticle(cornerParticles, rightBot, 1, 0.0, 0.0, 0.0, 0.0)
        owner.world.spawnParticle(cornerParticles, leftBot, 1, 0.0, 0.0, 0.0, 0.0)
        owner.world.spawnParticle(cornerParticles, rightTop, 1, 0.0, 0.0, 0.0, 0.0)

        if(editingPoint != null){
            val eyeLocation = owner.eyeLocation
            val placeLocation = eyeLocation.add(eyeLocation.direction.multiply(editingDistance))
            when(editingPoint){
                "leftTop" -> leftTop = placeLocation
                "rightTop" -> rightTop = placeLocation
                "leftBot" -> leftBot = placeLocation
                "rightBot" -> rightBot = placeLocation
            }
        }
    }

    fun getLocationAt(width: Double, height: Double): Location {
        val oneMinusWidth = 1.0 - width
        val oneMinusHeight = 1.0 - height

        val topLeft = leftTop.clone().multiply(oneMinusHeight* oneMinusHeight)
        val topRight = rightTop.clone().multiply(width * oneMinusHeight)
        val botLeft = leftBot.clone().multiply(oneMinusWidth * height)
        val botRight = rightBot.clone().multiply( width * height)

        return topLeft.add(topRight).add(botLeft).add(botRight)
    }

    override fun toString(): String {
        return "TEXTBOX!!!"
    }
}