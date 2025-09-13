package dev.arnagpal.dgenerator.utils.debug.shape

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.BYTE
import net.minestom.server.network.NetworkBuffer.COMPONENT

enum class DebugShapeTextAlignment(val id: Byte) {
    TOP_LEFT(0),
    TOP_RIGHT(1),
    BOTTOM_LEFT(2),
    BOTTOM_RIGHT(3),
    F3_LEFT(4),
    F3_RIGHT(5);
}

class DebugShapeGuiText(
    val components: List<Component>,
    val location: DebugShapeTextAlignment
) : DebugShape {

    override fun center(): Pos {
        return Pos.ZERO
    }

    override fun write(buf: NetworkBuffer) {
        buf.write(COMPONENT.list(), components)
        buf.write(BYTE, location.id)
    }

    override fun type(): String {
        return "gui_text"
    }
}