package dev.arnagpal.dgenerator.utils.debug

import dev.arnagpal.dgenerator.utils.debug.shape.DebugShape
import dev.arnagpal.dgenerator.utils.debug.shape.DebugShapeFlags
import net.kyori.adventure.key.Key
import net.minestom.server.entity.Player
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.NetworkBuffer.*
import net.minestom.server.network.packet.server.common.PluginMessagePacket

class DebugMessage {

    companion object {

        fun addShape(
            player: Player,
            namespace: String = "mft_debug",
            id: String,
            shape: DebugShape,
            flags: DebugShapeFlags = DebugShapeFlags.NONE,
            lifetime: Int = -1
        ) {
            val channel = "debugrender:add"
            val buf = NetworkBuffer.builder(1024).build()
            buf.write(KEY.optional(), Key.key(namespace, id))
            buf.write(STRING, shape.type())
            shape.write(buf) // Write the shape data
            buf.write(VAR_INT, flags.id)
            buf.write(VAR_INT, lifetime)
            val packet = PluginMessagePacket(channel, buf.read(RAW_BYTES))
            player.sendPacket(packet)
        }

    }

}