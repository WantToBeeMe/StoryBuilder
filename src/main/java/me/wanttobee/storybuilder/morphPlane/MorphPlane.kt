package me.wanttobee.storybuilder.morphPlane

import me.wanttobee.storybuilder.SBUtil.blockLocation
import me.wanttobee.storybuilder.SBUtil.rigidParticles
import org.bukkit.*
import org.bukkit.Particle.DustOptions


class MorphPlane(val world : World) {

    var curveFactor = 4.0
    var leftTop: Location? = null
        set(value) {
            value?.blockLocation()
            field = value
        }
    var rightTop: Location? = null
        set(value) {
            value?.blockLocation()
            field = value
        }
    var leftBottom: Location? = null
        set(value) {
            value?.blockLocation()
            field = value
        }
    var rightBottom: Location? = null
        set(value) {
            value?.blockLocation()
            field = value
        }


    private var topControlPoints : MutableList<Location> = mutableListOf()
    private var bottomControlPoints : MutableList<Location> = mutableListOf()
    private var leftControlPoints : MutableList<Location> = mutableListOf()
    private var rightControlPoints : MutableList<Location> = mutableListOf()
    fun setControlPoint(whichOne : String, index : Int, loc : Location){
        when(whichOne){
            "top" -> { topControlPoints[index] = loc.blockLocation() }
            "bottom" -> { bottomControlPoints[index] = loc.blockLocation() }
            "left" -> { leftControlPoints[index] = loc.blockLocation() }
            "right" -> { rightControlPoints[index] = loc.blockLocation() }
        }
    }
    fun addControlPoints(whichOne : String, index : Int, loc : Location){
        when(whichOne){
            "top" -> {topControlPoints.add(index,loc.blockLocation())}
            "bottom" -> {bottomControlPoints.add(index,loc.blockLocation())}
            "left" -> {leftControlPoints.add(index,loc.blockLocation())}
            "right" -> {rightControlPoints.add(index,loc.blockLocation())}
        }
    }
    fun getControlPoints(whichOne : String) : List<Location>{
       return when(whichOne){
            "top" -> topControlPoints
            "bottom" -> bottomControlPoints
            "left" -> leftControlPoints
            "right" -> rightControlPoints
            else -> emptyList()
       }
    }
    fun removeControlPoint(whichOne:String, index: Int){
        when(whichOne){
            "top" -> topControlPoints.removeAt(index)
            "bottom" -> bottomControlPoints.removeAt(index)
            "left" -> leftControlPoints.removeAt(index)
            "right" -> rightControlPoints.removeAt(index)
        }
    }

    fun getSideLength(whichOne:String) : Double{
        if(!this.isComplete()) return 0.0
        return when(whichOne){
            "top" -> {
                val controlPoints = listOf(leftTop!!) + topControlPoints + rightTop!!
                getSideLength(controlPoints)
            }
            "bottom" -> {
                val controlPoints = listOf(leftBottom!!) + bottomControlPoints + rightBottom!!
                getSideLength(controlPoints)
            }
            "left" -> {
                val controlPoints = listOf(leftTop!!) + leftControlPoints + leftBottom!!
                getSideLength(controlPoints)
            }
            "right" -> {
                val controlPoints = listOf(rightTop!!) + rightControlPoints + rightBottom!!
                getSideLength(controlPoints)
            }
            else -> {0.0}
        }

    }
    private fun getSideLength(controlPoints: List<Location>): Double{
        val samples = 200
        var oldPoint = controlPoints[0]
        var distance = 0.0
        for(i in 1 .. samples){
            val t = i/samples.toDouble()
            val newPoint = calculateBezierPoint(controlPoints,t)
            distance += oldPoint.distance(newPoint)
            oldPoint = newPoint
        }
        return distance
    }

    fun isComplete() : Boolean{
        return leftTop != null &&
                rightTop != null &&
                leftBottom != null &&
                rightBottom != null
    }

    fun rotate(right : Boolean){
        if(right){
            val tempLoc = leftBottom
            leftBottom = rightBottom
            rightBottom = rightTop
            rightTop = leftTop
            leftTop = tempLoc

            val tempList = bottomControlPoints
            bottomControlPoints = rightControlPoints
            rightControlPoints = topControlPoints
            topControlPoints = leftControlPoints
            leftControlPoints = tempList
        }else{
            val tempLoc = leftBottom
            leftBottom = leftTop
            leftTop = rightTop
            rightTop = rightBottom
            rightBottom = tempLoc

            val tempList = bottomControlPoints
            bottomControlPoints = leftControlPoints
            leftControlPoints = topControlPoints
            topControlPoints = rightControlPoints
            rightControlPoints = tempList
        }
    }
    fun flip(horizontal : Boolean){
        if(horizontal){
            var tempLoc = leftBottom
            leftBottom = rightBottom
            rightBottom = tempLoc
            tempLoc = leftTop
            leftTop = rightTop
            rightTop = tempLoc

            val tempList = leftControlPoints
            leftControlPoints = rightControlPoints
            rightControlPoints = tempList
        }
        else{
            var tempLoc = leftTop
            leftTop = leftBottom
            leftBottom = tempLoc
            tempLoc = rightTop
            rightTop = rightBottom
            rightBottom = tempLoc

            val tempList = topControlPoints
            topControlPoints = bottomControlPoints
            bottomControlPoints = tempList
        }
    }


    private var tick = 0
    fun tick(){
        tick++
        if(leftTop != null) leftTop!!.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(85,255,85), 0.5f))
        if(rightTop != null) rightTop!!.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,170,0), 0.5f))
        if(leftBottom != null)  leftBottom!!.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,85,255), 0.5f))
        if(rightBottom != null) rightBottom!!.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(85,255,255), 0.5f))

        for(loc in topControlPoints +
                bottomControlPoints +
                leftControlPoints +
                rightControlPoints){
            loc.rigidParticles(Particle.REDSTONE, DustOptions(Color.fromRGB(255,255,85), 0.5f))
        }

        val animationTicks = 90
        val lines = 8
        val animationT = Math.abs(((tick%animationTicks)/animationTicks.toDouble()) *2 -1)
        for(i in 0 .. lines){
            val upDown = interpolate(i/lines.toDouble(), animationT) ?: return
            val leftRight = interpolate(animationT,i/lines.toDouble()) ?: return
            world.spawnParticle(Particle.FLAME, upDown, 1,0.0,0.0,0.0,0.0)
            world.spawnParticle(Particle.FLAME, leftRight, 1,0.0,0.0,0.0,0.0)
        }
    }

    fun interpolate(width: Double, height: Double): Location? {
        // Calculate the interpolated points on the top and bottom edges
        if(!isComplete()) return null
        val top = calculateBezierPoint(listOf(leftTop!!) + topControlPoints + rightTop!!, width)
        val bottom = calculateBezierPoint(listOf(leftBottom!!) + bottomControlPoints + rightBottom!!, width)
        val left = calculateBezierPoint(listOf(leftTop!!) + leftControlPoints + leftBottom!!, height)
        val right = calculateBezierPoint(listOf(rightTop!!) + rightControlPoints + rightBottom!!, height)

        val linearTop = calculateBezierPoint(listOf(leftTop!!) + rightTop!!, width)
        val linearBottom =calculateBezierPoint(listOf(leftBottom!!) + rightBottom!!, width)
        val linearInterpolatedPoint = linearTop.clone().add(linearBottom.clone().subtract(linearTop).multiply(height))
        // Interpolate between the top and bottom edges
        val curvedInterpolatedHeight = top.clone().add(bottom.clone().subtract(top).multiply(height))
        val curvedInterpolatedWidth = left.clone().add(right.clone().subtract(left).multiply(width))
        val curvedInterpolatedPoint = curvedInterpolatedHeight.add(curvedInterpolatedWidth).multiply(0.5)

        val vector = curvedInterpolatedPoint.subtract(linearInterpolatedPoint)

        return linearInterpolatedPoint.add(vector.multiply(curveFactor))
    }

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
