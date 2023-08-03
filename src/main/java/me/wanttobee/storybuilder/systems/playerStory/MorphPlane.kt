package me.wanttobee.storybuilder.systems.playerStory

import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.SBUtil.rigidParticles
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player


class MorphPlane(owner : Player) {

    var curveFactor = 4.0
    private val world = owner.world
    private var leftTop: Location = owner.location.add(0.0,1.0,0.0).blockLocation()
    private var rightTop: Location = owner.location.add(-1.0,1.0,0.0).blockLocation()
    private var leftBottom: Location = owner.location.blockLocation()
    private var rightBottom: Location = owner.location.add(-1.0,0.0,0.0).blockLocation()
    fun setLeftTop(loc : Location){ leftTop = loc }
    fun setRightTop(loc : Location){ rightTop = loc }
    fun setLeftBottom(loc : Location){ leftBottom = loc }
    fun setRightBottom(loc : Location){ rightBottom = loc }

    private val topControlPoints : MutableList<Location> = mutableListOf()
    private val bottomControlPoints : MutableList<Location> = mutableListOf()
    private val leftControlPoints : MutableList<Location> = mutableListOf()
    private val rightControlPoints : MutableList<Location> = mutableListOf()

    private var editing : String? = null
    private var editingIndex = -1
    private var tick = 0

    //fun isEditing() : Boolean{
    //    return editing != null
    //}
    //fun getAllLocations() : List<Location> {
    //    return listOf(leftTop) +
    //            rightTop +
    //            leftBottom +
    //            rightBottom +
    //            //we first add the 4 main corners because those are more important, and therefore are in front of the list
    //            topControlPoints +
    //            bottomControlPoints +
    //            leftControlPoints +
    //            rightControlPoints
    //}

    fun tick(){
        tick++
        leftTop.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(85,255,85), 0.5f))
        rightTop.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,170,0), 0.5f))
        leftBottom.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,85,255), 0.5f))
        rightBottom.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(85,255,255), 0.5f))
        for(loc in topControlPoints +
                bottomControlPoints +
                leftControlPoints +
                rightControlPoints){
            loc.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,255,85), 0.5f))
        }
        if(editing == null){
            val animationTicks = 90
            val lines = 8
            val animationT = Math.abs(((tick%animationTicks)/animationTicks.toDouble()) *2 -1)
            for(i in 0 .. lines){
                val upDown = interpolate(i/lines.toDouble(), animationT)
                val leftRight = interpolate(animationT,i/lines.toDouble())
                world.spawnParticle(Particle.FLAME, upDown, 1,0.0,0.0,0.0,0.0)
                world.spawnParticle(Particle.FLAME, leftRight, 1,0.0,0.0,0.0,0.0)

                //val upDownExtreme = extremeInterpolate(i/lines.toDouble(), animationT)
                //val leftRightExtreme = extremeInterpolate(animationT,i/lines.toDouble())
                //world.spawnParticle(Particle.CRIT_MAGIC, upDownExtreme, 1,0.0,0.0,0.0,0.0)
                //world.spawnParticle(Particle.CRIT_MAGIC, leftRightExtreme, 1,0.0,0.0,0.0,0.0)
            }
        }
    }

    var pointsPlaced = false; private set
    private val replacedPointBlocks = mutableMapOf<Location, BlockData>()
    private fun replacePoint(loc : Location, data : BlockData){
        if(!replacedPointBlocks.containsKey(loc))
            replacedPointBlocks[loc] = loc.block.blockData
        world.setBlockData(loc, data)
    }
    fun undoPlacePoints(){
        for((loc, data) in replacedPointBlocks)
            world.setBlockData(loc, data)
        replacedPointBlocks.clear()
        pointsPlaced = false
    }
    fun placePoints(){
        pointsPlaced = true
        val yellowBlock =  Material.YELLOW_STAINED_GLASS.createBlockData()
        for(loc in topControlPoints +
                bottomControlPoints +
                leftControlPoints +
                rightControlPoints){
            replacePoint(loc,yellowBlock)
        }

        replacePoint(leftTop, Material.LIME_STAINED_GLASS.createBlockData())
        replacePoint(rightTop, Material.ORANGE_STAINED_GLASS.createBlockData())
        replacePoint(leftBottom, Material.MAGENTA_STAINED_GLASS.createBlockData())
        replacePoint(rightBottom, Material.CYAN_STAINED_GLASS.createBlockData())

    }

    fun addControlPoint(loc : Location) : String {
        val loc = loc.blockLocation()
        // Calculate distances from the given location to the four corner points
        val distances = mapOf(
                "leftTop" to loc.distanceSquared(leftTop),
                "rightTop" to loc.distanceSquared(rightTop),
                "leftBottom" to loc.distanceSquared(leftBottom),
                "rightBottom" to loc.distanceSquared(rightBottom)
        )
        val realDistances = mapOf(
                "leftControl" to (distances["leftTop"]!! + distances["leftBottom"]!!),
                "rightControl" to (distances["rightTop"]!! + distances["rightBottom"]!!),
                "bottomControl" to (distances["leftBottom"]!! + distances["rightBottom"]!!),
                "topControl" to (distances["leftTop"]!! + distances["rightTop"]!!)
        )
        val closestPointKey = realDistances.entries.sortedBy { it.value }.take(1)[0].key
        when(closestPointKey){
            "leftControl" -> {leftControlPoints.add(loc)}
            "rightControl" -> {rightControlPoints.add(loc)}
            "bottomControl" -> {bottomControlPoints.add(loc)}
            "topControl" -> {topControlPoints.add(loc)}
        }
        return "${ChatColor.YELLOW}${closestPointKey} point"
    }

    fun removeControlPoint(loc : Location) : String?{
        if(replacedPointBlocks.containsKey(loc)){
            world.setBlockData(loc, replacedPointBlocks[loc]!!)
            replacedPointBlocks.remove(loc)
        }
        if(leftTop == loc || leftBottom == loc || rightTop == loc || rightBottom == loc)
            return "Corner"
        var returnName : String? = null
        if(topControlPoints.contains(loc)){
            topControlPoints.remove(loc)
            returnName = "${ChatColor.YELLOW}topControl point"
        }
        else if(bottomControlPoints.contains(loc)){
            bottomControlPoints.remove(loc)
            returnName = "${ChatColor.YELLOW}bottomControl point"
        }
        else if(leftControlPoints.contains(loc)){
            leftControlPoints.remove(loc)
            returnName = "${ChatColor.YELLOW}leftControl point"
        }
        else if(rightControlPoints.contains(loc)){
            rightControlPoints.remove(loc)
            returnName = "${ChatColor.YELLOW}rightControl point"
        }
        return returnName
    }


    fun startEditing(point : Location?) : String?{
        if(point == null) return null
        var returnName : String? = null
        if(point == leftTop) {
            editing = "leftTop"
            returnName = "${ChatColor.GREEN}leftTop"
        }
        else if(point == rightTop) {
            editing = "rightTop"
            returnName = "${ChatColor.GOLD}rightTop"
        }
        else if(point == leftBottom) {
            editing = "leftBottom"
            returnName = "${ChatColor.LIGHT_PURPLE}leftBottom"
        }
        else if(point == rightBottom) {
            editing = "rightBottom"
            returnName = "${ChatColor.AQUA}rightBottom"
        }
        else if(topControlPoints.indexOf(point) >= 0){
            editing = "topControl"
            editingIndex = topControlPoints.indexOf(point)
            returnName = "${ChatColor.YELLOW}topControl point"
        }
        else if(bottomControlPoints.indexOf(point) >= 0){
            editing = "bottomControl"
            editingIndex = bottomControlPoints.indexOf(point)
            returnName = "${ChatColor.YELLOW}bottomControl point"
        }
        else if(leftControlPoints.indexOf(point) >= 0){
            editing = "leftControl"
            editingIndex = leftControlPoints.indexOf(point)
            returnName = "${ChatColor.YELLOW}leftControl point"
        }
        else if(rightControlPoints.indexOf(point) >= 0){
            editing = "rightControl"
            editingIndex = rightControlPoints.indexOf(point)
            returnName = "${ChatColor.YELLOW}rightControl point"
        }
        return returnName
    }
    fun stopEditing(point : Location){
        val point = point.blockLocation()
        if(editing == null) return
        when(editing!!){
            "leftTop" -> { leftTop = point }
            "rightTop" -> { rightTop = point }
            "leftBottom" -> { leftBottom = point }
            "rightBottom" -> { rightBottom = point }
            "topControl" -> { topControlPoints[editingIndex] = point }
            "bottomControl" -> { bottomControlPoints[editingIndex] = point }
            "leftControl" -> { leftControlPoints[editingIndex] = point }
            "rightControl" -> { rightControlPoints[editingIndex] = point }
        }
        editing = null
        undoPlacePoints()
    }

    fun interpolate(width: Double, height: Double): Location {
        // Calculate the interpolated points on the top and bottom edges
        val top = calculateBezierPoint(listOf(leftTop) + topControlPoints + rightTop, width)
        val bottom = calculateBezierPoint(listOf(leftBottom) + bottomControlPoints + rightBottom, width)
        val left = calculateBezierPoint(listOf(leftTop) + leftControlPoints + leftBottom, height)
        val right = calculateBezierPoint(listOf(rightTop) + rightControlPoints + rightBottom, height)

        val linearTop = calculateBezierPoint(listOf(leftTop) + rightTop, width)
        val linearBottom =calculateBezierPoint(listOf(leftBottom) + rightBottom, width)
        val linearInterpolatedPoint = linearTop.clone().add(linearBottom.clone().subtract(linearTop).multiply(height))
        // Interpolate between the top and bottom edges
        val curvedInterpolatedHeight = top.clone().add(bottom.clone().subtract(top).multiply(height))
        val curvedInterpolatedWidth = left.clone().add(right.clone().subtract(left).multiply(width))
        val curvedInterpolatedPoint = curvedInterpolatedHeight.add(curvedInterpolatedWidth).multiply(0.5)

        val vector = curvedInterpolatedPoint.subtract(linearInterpolatedPoint)

        return linearInterpolatedPoint.add(vector.multiply(curveFactor))
    }

   //fun extremeInterpolate(width: Double, height: Double): Location {
   //    val linearTop = calculateBezierPoint(listOf(leftTop) + rightTop, width)
   //    val linearBottom =calculateBezierPoint(listOf(leftBottom) + rightBottom, width)

   //    val linearInterpolatedPoint = linearTop.clone().add(linearBottom.clone().subtract(linearTop).multiply(height))
   //    val curvedVector = interpolate(width, height).subtract(linearInterpolatedPoint)
   //    return linearInterpolatedPoint.add(curvedVector.multiply(curveFactor))
   //}

    private fun calculateBezierPoint(controlPoints: List<Location>, t: Double): Location {
        if(controlPoints.isEmpty()) return Location(world,0.0,0.0,0.0)
        if (controlPoints.size == 1) return controlPoints[0]
        val n = controlPoints.size - 1
        var x = 0.0
        var y = 0.0
        var z = 0.0

        for (i in 0..n) {
            val binomialCoefficient = calculateBinomialCoefficient(n, i)
            val tPowI = Math.pow(t, i.toDouble())
            val tPowNMinusI = Math.pow(1 - t, (n - i).toDouble())
            val weight = binomialCoefficient * tPowI * tPowNMinusI

            val point = controlPoints[i]
            x += (point.x +0.5)* weight
            y += (point.y +0.5)* weight
            z += (point.z +0.5)* weight
        }
        return Location(world, x, y, z)
    }
    private fun calculateBinomialCoefficient(n: Int, k: Int): Int {
        if (k == 0 || k == n) {
            return 1
        }
        var result = 1
        for (i in 1..k) {
            result *= (n - i + 1)
            result /= i
        }
        return result
    }
}
