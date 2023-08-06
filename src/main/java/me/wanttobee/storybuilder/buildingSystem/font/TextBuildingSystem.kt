package me.wanttobee.storybuilder.buildingSystem.font

import me.wanttobee.storybuilder.SBPlugin
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
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D

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

    private fun preparingTextBuilder(playerStory: PlayersStory, plane : MorphPlane, word : String, font: Font){
        val firstChar = word[0]
        val glyphVector : GlyphVector = font.createGlyphVector(fontRenderContext, firstChar.toString())
        val outline = glyphVector.outline
        val path : PathIterator = outline.getPathIterator( AffineTransform() )
        val boundingBox = glyphVector.logicalBounds //glyphVector.visualBounds

        //val planeRatio : Double = when(playerStory.fontRatio){
        //    RatioMode.LEFT_TOP -> { plane.getSideLength("top") / plane.getSideLength("left") }
        //    RatioMode.RIGHT_TOP-> { plane.getSideLength("top") / plane.getSideLength("right") }
        //    RatioMode.LEFT_BOTTOM-> { plane.getSideLength("bottom") / plane.getSideLength("left") }
        //    RatioMode.RIGHT_BOTTOM-> { plane.getSideLength("bottom") / plane.getSideLength("right") }
        //    RatioMode.NONE-> {1.0}
        //}
        //val charRatio : Double = boundingBox.width / boundingBox.height

        playerStory.runBlockRecorderAsync { br ->
            var startPosition : Vector2d? = null
            var currentPosition = Vector2d(0.0,0.0)
            val point = DoubleArray(6)
            val center = transformPoint(boundingBox, boundingBox.centerX, boundingBox.centerY)
            while (!path.isDone) {
                val segment = path.currentSegment(point)
                transformPoints(boundingBox, point)
                when (segment){
                    PathIterator.SEG_MOVETO -> {
                        currentPosition = Vector2d(point[0], point[1])
                        if(startPosition == null)
                            startPosition = currentPosition
                    }
                    PathIterator.SEG_CLOSE -> {
                        if(startPosition != null){
                            val steps = 10
                            for(i in 0..steps){
                                val loc = lineTo(i/steps.toDouble(),plane, currentPosition, startPosition)
                                br.place(plane.interpolate(loc.x, loc.y)!!, Material.STONE.createBlockData())
                            }
                            currentPosition = startPosition
                        }
                        startPosition = null
                    }
                    PathIterator.SEG_LINETO -> {
                        val newCurLock =  Vector2d(point[0], point[1])
                        val steps = 100
                        for(i in 0..steps){
                            val loc = lineTo(i/steps.toDouble(),plane, currentPosition, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, Material.STONE.createBlockData())
                        }
                        currentPosition = newCurLock
                    }
                    PathIterator.SEG_QUADTO -> {
                        val newCurLock =  Vector2d(point[2], point[3])
                        val controlPoint =Vector2d(point[0], point[1])
                        val steps = 100
                        for(i in 0..steps){
                            val loc = quadTo(i/steps.toDouble(), currentPosition,controlPoint, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, Material.STONE.createBlockData())
                        }
                        currentPosition = newCurLock
                    }
                    PathIterator.SEG_CUBICTO -> {
                        val newCurLock =  Vector2d(point[4], point[5])
                        val controlPoint1 =Vector2d(point[0], point[1])
                        val controlPoint2 =Vector2d(point[2], point[3])
                        val steps = 100
                        for(i in 0..steps){
                            val loc = cubeTo(i/steps.toDouble(), currentPosition,controlPoint1,controlPoint2, newCurLock)
                            br.place(plane.interpolate(loc.x, loc.y)!!, Material.STONE.createBlockData())
                        }
                        currentPosition = newCurLock
                    }
                }
                path.next()
            }
            br.place( plane.interpolate(center.x+0.5, center.y+0.5 )!!, Material.REDSTONE_BLOCK.createBlockData() )
        }
    }

    private fun transformPoints(boundingBox : Rectangle2D, points : DoubleArray){
        val midCorrectionX = 0//(boundingBox.maxX - boundingBox.minX) / 2
        val midCorrectionY = 0//(boundingBox.maxY - boundingBox.minY) / 2
        val mulX = 1//1/boundingBox.width
        val mulY = 1//1/boundingBox.height
        for(i in 0 until 3){
            /*x*/points[i*2] =     (points[i*2]     -boundingBox.centerX + midCorrectionX) * mulX + 0.5
            /*y*/points[i*2 + 1] = (points[i*2 + 1] -boundingBox.centerY + midCorrectionY) * mulY + 0.5
        }
    }
    private fun transformPoint(boundingBox : Rectangle2D, x : Double, y :Double) : Vector2d{
        val midCorrectionX = 0//(boundingBox.maxX - boundingBox.minX) / 2
        val midCorrectionY = 0//(boundingBox.maxY - boundingBox.minY) / 2
       return Vector2d(
               x -boundingBox.centerX + midCorrectionX + 0.5,
               y -boundingBox.centerY + midCorrectionY + 0.5
       )
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