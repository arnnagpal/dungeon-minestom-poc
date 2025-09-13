package dev.arnagpal.dgenerator.utils.debug.shape

import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.*

class DebugShapeLineStrip(
    val points: List<Pos>,
    val argb: Int,
    val lineThickness: Float,
) : DebugShape {

    override fun center(): Pos {
        return if (points.isEmpty()) Pos.ZERO else points[0]
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(VECTOR3D.list(), points)
        buf.write(INT, argb)
        buf.write(FLOAT, lineThickness)
    }

    override fun type(): String {
        return "line_strip"
    }
}