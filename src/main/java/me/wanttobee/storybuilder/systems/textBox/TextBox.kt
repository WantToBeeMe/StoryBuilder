package me.wanttobee.storybuilder.systems.textBox

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
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
    private var editingIndex : Int = -1
    private var editingDistance = 4

    init{
        this.leftTop = owner.location.add(-2.0, 2.0, 0.0)
        this.rightTop = owner.location.add(2.0, 2.0, 0.0)
        this.leftBot = owner.location.add(-2.0, -1.0, 0.0)
        this.rightBot = owner.location.add(2.0, -1.0, 0.0)
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
        if(corner == leftTop) editingPoint = "leftTop"
        else if(corner == rightTop) editingPoint = "rightTop"
        else if (corner == leftBot) editingPoint = "leftBot"
        else if(corner == rightBot) editingPoint = "rightBot"
        else if(topControlPoints.contains(corner) ) {
            editingPoint = "top"
            editingIndex = topControlPoints.indexOf(corner)
        }
        else if(botControlPoints.contains(corner) ) {
            editingPoint = "bot"
            editingIndex = botControlPoints.indexOf(corner)
        }
        else if(leftControlPoints.contains(corner) ) {
            editingPoint = "left"
            editingIndex = leftControlPoints.indexOf(corner)
        }
        else if(rightControlPoints.contains(corner) ) {
            editingPoint = "right"
            editingIndex = rightControlPoints.indexOf(corner)
        }
        else editingPoint = null
    }

    private var tick = 0
    fun everyTick(){
        if(leftTop.world != owner.world) return

        tick++
        if(editingPoint != null){
            val eyeLocation = owner.eyeLocation
            val placeLocation = eyeLocation.add(eyeLocation.direction.multiply(editingDistance))
            when(editingPoint){
                "leftTop" -> leftTop = placeLocation
                "rightTop" -> rightTop = placeLocation
                "leftBot" -> leftBot = placeLocation
                "rightBot" -> rightBot = placeLocation
                "top" -> topControlPoints[editingIndex] = placeLocation
                "bot" -> botControlPoints[editingIndex] = placeLocation
                "left" -> leftControlPoints[editingIndex] = placeLocation
                "right" -> rightControlPoints[editingIndex] = placeLocation
            }
        }

        val cornerParticles =  Particle.REDSTONE// Change this to the particle you want to summon
        val cornerColor = DustOptions(Color.fromRGB(255, 235, 190), 4f)
        val smallColor = DustOptions(Color.fromRGB(245, 215, 165), 2f)

        owner.world.spawnParticle(cornerParticles, leftTop, 1, 0.0, 0.0, 0.0, 0.0, cornerColor)
        owner.world.spawnParticle(cornerParticles, rightBot, 1, 0.0, 0.0, 0.0, 0.0, cornerColor)
        owner.world.spawnParticle(cornerParticles, leftBot, 1, 0.0, 0.0, 0.0, 0.0, cornerColor)
        owner.world.spawnParticle(cornerParticles, rightTop, 1, 0.0, 0.0, 0.0, 0.0, cornerColor)

        for(loc in topControlPoints) owner.world.spawnParticle(cornerParticles, loc, 1, 0.0, 0.0, 0.0, 0.0, smallColor)
        for(loc in botControlPoints) owner.world.spawnParticle(cornerParticles, loc, 1, 0.0, 0.0, 0.0, 0.0, smallColor)
        for(loc in leftControlPoints) owner.world.spawnParticle(cornerParticles, loc, 1, 0.0, 0.0, 0.0, 0.0, smallColor)
        for(loc in rightControlPoints) owner.world.spawnParticle(cornerParticles, loc, 1, 0.0, 0.0, 0.0, 0.0, smallColor)

        val animationTick = 70.0
        val particleAmount = 10
        val animationPercentage = Math.abs( ((tick%animationTick)/animationTick)*2-1 )
        for(i in 0..particleAmount){
            owner.world.spawnParticle(Particle.FLAME, getLocationAt( animationPercentage,i/particleAmount.toDouble()), 1, 0.0, 0.0, 0.0, 0.0)
            owner.world.spawnParticle(Particle.FLAME, getLocationAt(i/particleAmount.toDouble(),  animationPercentage), 1, 0.0, 0.0, 0.0, 0.0)

            owner.world.spawnParticle(Particle.CRIT, getLocationAt( animationPercentage,i/particleAmount.toDouble()), 1, 0.0, 0.0, 0.0, 0.0)
            owner.world.spawnParticle(Particle.CRIT, getLocationAt(i/particleAmount.toDouble(),  animationPercentage), 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    fun getLocationAt(width: Double, height: Double): Location {
        val top = leftTop.clone().add((rightTop.x - leftTop.x)*width, (rightTop.y - leftTop.y)*width, (rightTop.z - leftTop.z)*width)
        val bot = leftBot.clone().add((rightBot.x - leftBot.x)*width, (rightBot.y - leftBot.y)*width, (rightBot.z - leftBot.z)*width)
        //val left = leftTop.clone().add((leftBot.x - leftTop.x)*height, (leftBot.y - leftTop.y)*height, (leftBot.z - leftTop.z)*height)
        //val right = rightTop.clone().add((rightBot.x - rightTop.x)*height, (rightBot.y - rightTop.y)*height, (rightBot.z - rightTop.z)*height)
        val horizontal = top.clone().add((bot.x - top.x)*height, (bot.y - top.y)*height, (bot.z - top.z)*height)
        //val vertical = left.clone().add(( right.x - left.x)*width,(right.y - left.y)*width,(right.z - left.z)*width )
        return horizontal
    }

    override fun toString(): String {
        return "TEXTBOX!!!"
    }
}