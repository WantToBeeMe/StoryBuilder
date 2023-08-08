package me.wanttobee.storybuilder.buildingSystem.font

import me.wanttobee.storybuilder.SBPlugin
import me.wanttobee.storybuilder.buildingSystem.ClampMode
import me.wanttobee.storybuilder.buildingSystem.ClampSides
import me.wanttobee.storybuilder.buildingSystem.buildingMenus.Alignment
import me.wanttobee.storybuilder.buildingSystem.buildingMenus.FontBuildingMenu
import me.wanttobee.storybuilder.commands.ISystemCommand
import me.wanttobee.storybuilder.commands.commandTree.CommandStringLeaf
import me.wanttobee.storybuilder.commands.commandTree.ICommandBranch
import me.wanttobee.storybuilder.morphPlane.MorphPlane
import me.wanttobee.storybuilder.playerStory.PlayersStory
import me.wanttobee.storybuilder.playerStory.StorySystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.joml.Vector2d
import java.awt.Font
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator

object TextBuildingSystem {
    private val plugin = SBPlugin.instance
    private val fontRenderContext = FontRenderContext(AffineTransform(), true, true)

    fun textBuilderCommand(player : Player, word : String){
        val playerStory = StorySystem.getPlayersStory(player)
        val plane = playerStory.morphPlane ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}you need a plane for this action")
            return
        }
        if(!plane.isComplete()){
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}the plane isn't complete")
            return
        }
        val font: Font = playerStory.getCurrentFont() ?: run {
            player.sendMessage("${SBPlugin.title}${ChatColor.RED}there is no font active, ${ChatColor.GRAY}/sb font [fontName] ${ChatColor.RED}to active one")
            return
        }

        FontBuildingMenu(player) {
            preparingTextBuilder(playerStory,plane, word,font)
        }.open(player)
    }

    private fun preparingTextBuilder(playerStory: PlayersStory, plane : MorphPlane, word : String, font: Font) {
        val stepMultiplier = 1.75
        val firstChar = word[0]
        val glyphVector: GlyphVector = font.createGlyphVector(fontRenderContext, firstChar.toString())
        val outline: Shape = glyphVector.outline
        val path: PathIterator = outline.getPathIterator(AffineTransform())

        val lines : MutableList<Line> = mutableListOf()
        var startPosition: Vector2d? = null
        var currentPosition = Vector2d(0.0, 0.0)
        val point = DoubleArray(6)
        while(!path.isDone){
            val segment = path.currentSegment(point)
            transformPoints(playerStory, glyphVector, point)
            when (segment) {
                PathIterator.SEG_MOVETO -> {
                    currentPosition = Vector2d(point[0], point[1])
                    if (startPosition == null)
                        startPosition = currentPosition
                }
                PathIterator.SEG_CLOSE -> {
                    if (startPosition != null) {
                        lines.add(Line(arrayOf(currentPosition, startPosition)))
                        currentPosition = startPosition
                    }
                    startPosition = null
                }
                PathIterator.SEG_LINETO -> {
                    val newCurLock = Vector2d(point[0], point[1])
                    lines.add(Line(arrayOf(currentPosition, newCurLock)))
                    currentPosition = newCurLock
                }
                PathIterator.SEG_QUADTO -> {
                    val newCurLock = Vector2d(point[2], point[3])
                    val controlPoint = Vector2d(point[0], point[1])
                    lines.add(Line(arrayOf(currentPosition, controlPoint, newCurLock)))
                    currentPosition = newCurLock
                }
                PathIterator.SEG_CUBICTO -> {
                    val newCurLock = Vector2d(point[4], point[5])
                    val controlPoint1 = Vector2d(point[0], point[1])
                    val controlPoint2 = Vector2d(point[2], point[3])
                    lines.add(Line(arrayOf(currentPosition, controlPoint1, controlPoint2, newCurLock)))
                    currentPosition = newCurLock
                }
            }
            path.next()
        }

        playerStory.runBlockRecorderAsync { br ->

            val edgeBlock = playerStory.primaryGradient.get(0).createBlockData()
            val fillBlock = playerStory.primaryGradient.get(2).createBlockData()
            val boundingBox = if (playerStory.fontLogicBoundingBox) glyphVector.logicalBounds else glyphVector.visualBounds
            val xSteps = (playerStory.samples*boundingBox.width *stepMultiplier).toInt()
            val ySteps = (playerStory.samples*boundingBox.height *stepMultiplier).toInt()

            val from = doubleArrayOf(boundingBox.minX, boundingBox.minY)
            transformPoints(playerStory, glyphVector, from)
            val fromVector = Vector2d(from[0], from[1])

            if (playerStory.fontFill) {
                for (x in 0..xSteps) {
                    for (y in 0.. ySteps) {
                        val intersectionPoint = doubleArrayOf(boundingBox.minX + boundingBox.width * (x / xSteps.toDouble()), boundingBox.minY + boundingBox.height * (y / ySteps.toDouble()))
                        transformPoints(playerStory, glyphVector, intersectionPoint)
                        //plugin.logger.info("from: ${fromVector.x} and ${fromVector.y}")
                        //plugin.logger.info("intersection: ${intersectionPoint[0]} and ${intersectionPoint[1]}")
                        var intersections = 0
                        for(l in lines)
                            intersections += l.intersectionAmount( fromVector,Vector2d(intersectionPoint[0], intersectionPoint[1]) )
                        //plugin.logger.info("amount: $intersections")
                        if (intersections % 2 == 1)
                            br.place(plane.interpolateLocation(intersectionPoint[0], intersectionPoint[1])!!, fillBlock)
                    }
                }
            }

            for(l in lines){
                val steps = (playerStory.samples * l.simpleDistance() *stepMultiplier).toInt()
                for (i in 0..steps) {
                    val loc = l.pointAtT(i / steps.toDouble())
                    br.place(plane.interpolateLocation(loc.x, loc.y)!!, edgeBlock)
                }
            }

            val customPoints = doubleArrayOf(
                    boundingBox.centerX, boundingBox.centerY,
                    boundingBox.minX, boundingBox.minY,
                    boundingBox.maxX, boundingBox.maxY)
            transformPoints(playerStory, glyphVector, customPoints)
            br.place(plane.interpolateLocation(customPoints[0], customPoints[1])!!, Material.REDSTONE_BLOCK.createBlockData())

            br.place(plane.interpolateLocation(customPoints[2], customPoints[3])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolateLocation(customPoints[2], customPoints[5])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolateLocation(customPoints[4], customPoints[3])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolateLocation(customPoints[4], customPoints[5])!!, Material.DIAMOND_BLOCK.createBlockData())

        }
    }

    private fun transformPoints(playerStory: PlayersStory, glyphVector : GlyphVector, points : DoubleArray) {
        val boundingBox = if (playerStory.fontLogicBoundingBox) glyphVector.logicalBounds else glyphVector.visualBounds
        val plane = playerStory.getPlane()

        val clampMode = playerStory.fontClampMode
        val charAspect = boundingBox.width / boundingBox.height
        val mul: Vector2d = if(clampMode == ClampMode.NONE) Vector2d(1 / boundingBox.width, 1 / boundingBox.height)
        else {
            val sideNames = when (playerStory.fontClampSide) {
                ClampSides.LEFT_OR_TOP -> Pair("left","top")
                ClampSides.LEFT_OR_BOTTOM -> Pair("left","bottom")
                ClampSides.RIGHT_OR_TOP -> Pair("right","top")
                ClampSides.RIGHT_OR_BOTTOM -> Pair("right","bottom")
            }
            val planeAspect = plane.getSideLength(sideNames.second) / plane.getSideLength(sideNames.first)
            if(clampMode == ClampMode.HEIGHT || (clampMode == ClampMode.AUTO && planeAspect > charAspect )){
                val mul = 1 / boundingBox.height
                val otherMul = (( mul * boundingBox.width * plane.getSideLength(sideNames.first) ) / plane.getSideLength(sideNames.second)) / boundingBox.width
                Vector2d(otherMul, mul )
            }
            else{
                val mul = 1 / boundingBox.width
                val otherMul = (( mul * boundingBox.height * plane.getSideLength(sideNames.second) ) / plane.getSideLength(sideNames.first)) / boundingBox.height
                Vector2d(mul, otherMul)
            }
        }

        val cX = boundingBox.centerX
        val cY = boundingBox.centerY
        val centerCorrectionX = -(boundingBox.maxX - cX + (boundingBox.minX - cX)) / 2
        val centerCorrectionY = -(boundingBox.maxY - cY + (boundingBox.minY - cY)) / 2

        val alignment: Vector2d = when (playerStory.alignment) {
            Alignment.CENTERED -> { Vector2d(0.5, 0.5)}
            Alignment.LEFT_TOP -> { Vector2d( boundingBox.width * 0.5 * mul.x, boundingBox.height * 0.5 * mul.y)}
            Alignment.LEFT_BOTTOM -> { Vector2d(boundingBox.width * 0.5 * mul.x, 1- boundingBox.height * 0.5 * mul.y)}
            Alignment.RIGHT_TOP -> { Vector2d(1- boundingBox.width * 0.5 * mul.x, boundingBox.height * 0.5 * mul.y)}
            Alignment.RIGHT_BOTTOM -> { Vector2d(1- boundingBox.width * 0.5 * mul.x, 1- boundingBox.height * 0.5 * mul.y)}
            Alignment.CENTERED_TOP -> { Vector2d( 0.5, boundingBox.height * 0.5 * mul.y)}
            Alignment.CENTERED_BOTTOM -> { Vector2d( 0.5, 1- boundingBox.height * 0.5 * mul.y)}
            Alignment.CENTERED_LEFT -> { Vector2d( boundingBox.width * 0.5 * mul.x, 0.5)}
            Alignment.CENTERED_RIGHT -> { Vector2d( 1-boundingBox.width * 0.5 * mul.x,0.5)}
        }
        playerStory.useFontSize
        playerStory.fontSize
        playerStory.fontOutOfBound

        for (i in 0 until points.size / 2) {
            /*x*/points[i * 2] = (points[i * 2] - cX + centerCorrectionX) * mul.x + alignment.x
            /*y*/points[i * 2 + 1] = (points[i * 2 + 1] - cY + centerCorrectionY) * mul.y + alignment.y
        }
    }

    object Text : ISystemCommand {
        override val exampleCommand: String = "/sd text [word/String]"
        override val helpText: String = "to generate a word or characters"
        override val baseTree: ICommandBranch = CommandStringLeaf("text", null, { player, value -> textBuilderCommand(player, value) })
    }

    class Line(private val points : Array<Vector2d>) {
        private val quadDetail = 5
        private val cubeDetail = 8

        private val intersectionLinePoints: Array<Vector2d>
        init{
            val detail = if(points.size == 3) quadDetail else cubeDetail
            intersectionLinePoints = if(points.size <= 2) points
            else Array(detail+1) {i -> pointAtT(i/detail.toDouble() ) }
        }

        fun simpleDistance() : Double{
            if(points.size == 1) return 0.0
            var dis = 0.0
            for(i in 1 until points.size){
                val dx = (points[i].x - points[i-1].x) * (points[i].x - points[i-1].x)
                val dy = (points[i].y - points[i-1].y) * (points[i].y - points[i-1].y)
                dis += Math.sqrt(dx + dy)
            }
            return dis
        }
        fun pointAtT(t : Double) : Vector2d{
            if(points.size == 2){
                val x = points[0].x + (points[1].x - points[0].x) * t
                val y = points[0].y + (points[1].y - points[0].y) * t
                return Vector2d(x, y)
            }
            if(points.size == 3){
                val x = (1 - t) * (1 - t) * points[0].x + 2 * (1 - t) * t * points[1].x + t * t * points[2].x
                val y = (1 - t) * (1 - t) * points[0].y + 2 * (1 - t) * t * points[1].y + t * t * points[2].y
                return Vector2d(x,y)
            }
            if(points.size == 4){
                val x = (1 - t) * (1 - t) * (1 - t) * points[0].x +
                        3 * (1 - t) * (1 - t) * t * points[1].x +
                        3 * (1 - t) * t * t * points[2].x +
                        t * t * t * points[3].x
                val y = (1 - t) * (1 - t) * (1 - t) * points[0].y +
                        3 * (1 - t) * (1 - t) * t * points[1].y +
                        3 * (1 - t) * t * t * points[2].y +
                        t * t * t * points[3].y
                return Vector2d(x,y)
            }
            return points[0]
        }
    
        fun intersectionAmount(start: Vector2d, end: Vector2d): Int {
            var amount = 0
            for(i in 1 until intersectionLinePoints.size){
                val intersects = doSegmentsIntersect(start,end, intersectionLinePoints[i-1],intersectionLinePoints[i])
                if(intersects)
                    amount++
            }
            return amount
        }


    }

    fun doSegmentsIntersect(startA: Vector2d, endA: Vector2d, startB: Vector2d, endB: Vector2d): Boolean {
        val det = (endA.x - startA.x) * (startB.y - endB.y) - (startB.x - endB.x) * (endA.y - startA.y)
        if (det == 0.0) return false // Lines are parallel or coincident, no intersection within segments

        val t = ((startB.x - startA.x) * (startB.y - endB.y) - (startB.y - startA.y) * (startB.x - endB.x)) / det
        val u = -((startA.x - endA.x) * (startB.y - startA.y) - (startA.y - endA.y) * (startB.x - startA.x)) / det

        return (t in 0.0..1.0) && (u in 0.0..1.0)
    }
}