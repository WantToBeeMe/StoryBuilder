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
        val firstChar = word[0]
        val glyphVector: GlyphVector = font.createGlyphVector(fontRenderContext, firstChar.toString())
        val outline: Shape = glyphVector.outline
        val path: PathIterator = outline.getPathIterator(AffineTransform())

        playerStory.runBlockRecorderAsync { br ->
            var startPosition: Vector2d? = null
            var currentPosition = Vector2d(0.0, 0.0)
            val point = DoubleArray(6)
            val steps = playerStory.samples
            val block = playerStory.currentGradient.get(1).createBlockData()

            if (playerStory.fontFill) {
                for (x in 0..steps) {
                    for (y in 0..steps) {
                        val boundingBox = if (playerStory.fontLogicBoundingBox) glyphVector.logicalBounds else glyphVector.visualBounds
                        val point = doubleArrayOf(boundingBox.minX + boundingBox.width * (x / steps), boundingBox.minY + boundingBox.height * (y / steps))
                        //(point[0], point[1])
                        if (false) {
                            //step 1, clone the path and loop over the points
                            //step 2, record the points and make the lines (or maybe later)
                            //step 3, check if this point, going from the top down-words (or bot, what is closest) intersects an odd amount, then fill it
                            transformPoints(playerStory, glyphVector, point)
                            br.place(plane.interpolate(point[0], point[1])!!, block)
                        }
                    }
                }
            }

            while (!path.isDone) {
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
                            val steps = steps / 10
                            for (i in 0..steps) {
                                val loc = lineTo(i / steps.toDouble(), plane, currentPosition, startPosition)
                                br.place(plane.interpolate(loc.x, loc.y)!!, block)
                            }
                            currentPosition = startPosition
                        }
                        startPosition = null
                    }

                    PathIterator.SEG_LINETO -> {
                        val newCurLock = Vector2d(point[0], point[1])
                        for (i in 0..steps) {
                            val loc = lineTo(i / steps.toDouble(), plane, currentPosition, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, block)
                        }
                        currentPosition = newCurLock
                    }

                    PathIterator.SEG_QUADTO -> {
                        val newCurLock = Vector2d(point[2], point[3])
                        val controlPoint = Vector2d(point[0], point[1])
                        for (i in 0..steps) {
                            val loc = quadTo(i / steps.toDouble(), currentPosition, controlPoint, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, block)
                        }
                        currentPosition = newCurLock
                    }

                    PathIterator.SEG_CUBICTO -> {
                        val newCurLock = Vector2d(point[4], point[5])
                        val controlPoint1 = Vector2d(point[0], point[1])
                        val controlPoint2 = Vector2d(point[2], point[3])
                        for (i in 0..steps) {
                            val loc = cubeTo(i / steps.toDouble(), currentPosition, controlPoint1, controlPoint2, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, block)
                        }
                        currentPosition = newCurLock
                    }
                }
                path.next()
            }
            val boundingBox = if (playerStory.fontLogicBoundingBox) glyphVector.logicalBounds else glyphVector.visualBounds
            val customPoints = doubleArrayOf(
                    boundingBox.centerX, boundingBox.centerY,
                    boundingBox.minX, boundingBox.minY,
                    boundingBox.maxX, boundingBox.maxY)
            transformPoints(playerStory, glyphVector, customPoints)
            br.place(plane.interpolate(customPoints[0], customPoints[1])!!, Material.REDSTONE_BLOCK.createBlockData())

            br.place(plane.interpolate(customPoints[2], customPoints[3])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolate(customPoints[2], customPoints[5])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolate(customPoints[4], customPoints[3])!!, Material.DIAMOND_BLOCK.createBlockData())
            br.place(plane.interpolate(customPoints[4], customPoints[5])!!, Material.DIAMOND_BLOCK.createBlockData())

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



    private fun distance(from: Vector2d, to: Vector2d) : Int{
        val dx = (to.x - from.x) * (to.x - from.x)
        val dy = (to.y - from.y) * (to.y - from.y)
        return Math.ceil(Math.sqrt((dx + dy))).toInt()
    }
    private fun lineTo(t : Double, plane : MorphPlane, start: Vector2d, end: Vector2d): Vector2d {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val x = start.x + dx * t
        val y = start.y + dy * t
        return Vector2d(x, y)
    }
    private fun quadTo(t : Double, start: Vector2d, control: Vector2d, end: Vector2d): Vector2d {
        val x = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
        val y = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y
        return Vector2d(x,y)
    }
    private fun cubeTo(t : Double, start: Vector2d, control1: Vector2d, control2: Vector2d, end: Vector2d) : Vector2d {
        val x = (1 - t) * (1 - t) * (1 - t) * start.x +
                3 * (1 - t) * (1 - t) * t * control1.x +
                3 * (1 - t) * t * t * control2.x +
                t * t * t * end.x
        val y = (1 - t) * (1 - t) * (1 - t) * start.y +
                3 * (1 - t) * (1 - t) * t * control1.y +
                3 * (1 - t) * t * t * control2.y +
                t * t * t * end.y
        return Vector2d(x,y)
    }

    object Text : ISystemCommand {
        override val exampleCommand: String = "/sd text [word/String]"
        override val helpText: String = "to generate a word or characters"
        override val baseTree: ICommandBranch = CommandStringLeaf("text", null, { player, value -> textBuilderCommand(player, value) })
    }
}