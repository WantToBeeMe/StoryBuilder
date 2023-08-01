package me.wanttobee.storybuilder.systems
import me.wanttobee.storybuilder.SBPlugin
import org.bukkit.entity.Player
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator
import org.bukkit.Location;
import org.bukkit.Material

object FontBuildingSystem {
    private val plugin = SBPlugin.instance
    private val fontRenderContext = FontRenderContext(AffineTransform(), true, true)

    private const val size = 30

    fun buildSentence(commander:Player, sentence : List<String>, font : Font){
        buildWord(commander,sentence[0],font)
    }

    fun buildWord(commander: Player, word: String, font : Font){
        val firstChar = word[0]
        val glyphVector = font.createGlyphVector(fontRenderContext, firstChar.toString())
        val outline = glyphVector.outline
        val pathIterator : PathIterator = outline.getPathIterator( AffineTransform() )
        buildChar(commander, pathIterator)
    }

    fun buildChar(commander: Player, path: PathIterator){
        val point = FloatArray(6)
        var startPosition: Location? = null
        var currentPosition = commander.location

        while (!path.isDone) {
            val segmentType = path.currentSegment(point)


            when (segmentType){
                PathIterator.SEG_MOVETO -> {
                    currentPosition = commander.location.add(point[0].toDouble() * size,-point[1].toDouble() * size, 0.0)
                    if(startPosition == null) startPosition = currentPosition
                    startPosition.world!!.setBlockData(startPosition, Material.REDSTONE_BLOCK.createBlockData())
                }
                PathIterator.SEG_CLOSE -> {
                    if(startPosition != null){
                        lineTo(currentPosition,startPosition)
                        startPosition.world!!.setBlockData(startPosition, Material.REDSTONE_BLOCK.createBlockData())
                    }
                    startPosition = null
                }
                PathIterator.SEG_LINETO -> {
                    val oldPos = currentPosition.clone()
                    currentPosition = commander.location.add(point[0].toDouble() * size,-point[1].toDouble() * size, 0.0)
                    lineTo(oldPos,currentPosition)
                }
                PathIterator.SEG_QUADTO -> {
                    val oldPos = currentPosition.clone()
                    val controlPoint = commander.location.add(point[0].toDouble() * size,-point[1].toDouble() * size, 0.0)
                    currentPosition = commander.location.add(point[2].toDouble() * size,-point[3].toDouble() * size, 0.0)
                    quadTo(oldPos,controlPoint,currentPosition)
                }
                PathIterator.SEG_CUBICTO -> {
                    val oldPos = currentPosition.clone()
                    val controlPoint1 = commander.location.add(point[0].toDouble() * size,-point[1].toDouble() * size, 0.0)
                    val controlPoint2 = commander.location.add(point[2].toDouble() * size,-point[3].toDouble() * size, 0.0)
                    currentPosition = commander.location.add(point[4].toDouble() * size,-point[5].toDouble() * size, 0.0)
                    cubeTo(oldPos,controlPoint1,controlPoint2,currentPosition)
                }
            }
            path.next()
        }
    }


    fun distance(from: Location, to:Location) : Int{
        val dx = (to.x - from.x) * (to.x - from.x)
        val dy = (to.y - from.y) * (to.y - from.y)
        val dz = (to.z - from.z) * (to.z - from.z)
        return Math.ceil(Math.sqrt(dx + dy + dz)).toInt()
    }

    fun lineTo(start: Location, end: Location) {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val dz = end.z - start.z
        val steps = distance(start, end)

        for (i in 0..steps) {
            val x = start.x + dx * i / steps
            val y = start.y + dy * i / steps
            val z = start.z + dz * i / steps


            start.world!!.setBlockData(x.toInt(), y.toInt(), z.toInt() , Material.STONE.createBlockData() )
        }
    }

    fun quadTo(start: Location, control: Location, end: Location) {
        val steps = distance(start, control) + distance(control, end)

        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
            val y = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y
            val z = (1 - t) * (1 - t) * start.z + 2 * (1 - t) * t * control.z + t * t * end.z

            start.world!!.setBlockData(x.toInt(), y.toInt(), z.toInt() , Material.STONE.createBlockData() )
        }
    }

    fun cubeTo(start: Location, control1: Location, control2: Location, end: Location) {
        val steps = distance(start, control1) + distance(control1, control2) + distance(control2, end)

        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = (1 - t) * (1 - t) * (1 - t) * start.x +
                    3 * (1 - t) * (1 - t) * t * control1.x +
                    3 * (1 - t) * t * t * control2.x +
                    t * t * t * end.x
            val y = (1 - t) * (1 - t) * (1 - t) * start.y +
                    3 * (1 - t) * (1 - t) * t * control1.y +
                    3 * (1 - t) * t * t * control2.y +
                    t * t * t * end.y
            val z = (1 - t) * (1 - t) * (1 - t) * start.z +
                    3 * (1 - t) * (1 - t) * t * control1.z +
                    3 * (1 - t) * t * t * control2.z +
                    t * t * t * end.z

            start.world!!.setBlockData(x.toInt(), y.toInt(), z.toInt() , Material.STONE.createBlockData() )
        }
    }



}