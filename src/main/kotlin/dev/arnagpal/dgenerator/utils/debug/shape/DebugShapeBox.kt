package dev.arnagpal.dgenerator.utils.debug.shape

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.*
import team.unnamed.hephaestus.util.Quaternion

class DebugShapeBox(
    val center: Point,
    val size: Point,
    val rotation: Quaternion,
    val faceArgb: Int,
    val lineArgb: Int,
    val lineThickness: Float,
) : DebugShape {

    override fun center(): Pos {
        return center.asPos()
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(VECTOR3D, center)
        buf.write(VECTOR3D, size)
        buf.write(QUATERNION, rotation.toFloatArray())
        buf.write(INT, faceArgb)
        buf.write(INT, lineArgb)
        buf.write(FLOAT, lineThickness)
    }

    override fun type(): String {
        return "box"
    }
}