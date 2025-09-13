package dev.arnagpal.dgenerator.utils.debug.shape

import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer

enum class DebugShapeFlags(val id: Int) {
    NONE(0),
    FLAG_SHOW_THROUGH_WALLS(1),
    FLAG_FULL_OPACITY_BEHIND_WALLS(2),
    FLAG_WIREFRAME(4),
    FLAG_NO_SHADE(8),
    FLAG_SHOW_AXIS(16);

    companion object {
        private val map = entries.associateBy(DebugShapeFlags::id)
        fun fromId(id: Int) = map[id] ?: NONE
    }
}

interface DebugShape {
    fun center(): Pos

    fun write(buf: NetworkBuffer)

    fun type(): String
}