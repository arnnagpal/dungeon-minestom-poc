package dev.arnagpal.dgenerator.utils.debug.shape

import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.INT
import net.minestom.server.network.NetworkBuffer.VECTOR3D

class DebugShapeQuad(
    val one: Pos,
    val two: Pos,
    val three: Pos,
    val four: Pos,
    val argb: Int,
) : DebugShape {

    override fun center(): Pos {
        return one
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(VECTOR3D, one)
        buf.write(VECTOR3D, two)
        buf.write(VECTOR3D, three)
        buf.write(VECTOR3D, four)
        buf.write(INT, argb)
    }

    override fun type(): String {
        return "quad"
    }
}