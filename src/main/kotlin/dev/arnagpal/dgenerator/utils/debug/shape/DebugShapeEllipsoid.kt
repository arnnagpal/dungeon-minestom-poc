package dev.arnagpal.dgenerator.utils.debug.shape

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.*
import team.unnamed.hephaestus.util.Quaternion

class DebugShapeEllipsoid(
    val center: Pos,
    val size: Vec,
    val rotation: Quaternion,
    val argb: Int,
    val detail: Int,
) : DebugShape {

    override fun center(): Pos {
        return center
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(VECTOR3D, center)
        buf.write(VECTOR3D, size)
        buf.write(QUATERNION, rotation.toFloatArray())
        buf.write(INT, argb)
        buf.write(VAR_INT, detail)
    }

    override fun type(): String {
        return "ellipsoid"
    }
}