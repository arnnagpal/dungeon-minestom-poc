package dev.arnagpal.dgenerator.utils.debug.shape

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.*

class DebugShapeText(
    val position: Pos,
    val component: Component,
    val shadow: Boolean,
    val backgroundColour: Int,
) : DebugShape {

    override fun center(): Pos {
        return position
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(VECTOR3D, position)
        buf.write(COMPONENT, component)
        buf.write(BOOLEAN, shadow)
        buf.write(INT, backgroundColour)
    }

    override fun type(): String {
        return "text"
    }
}