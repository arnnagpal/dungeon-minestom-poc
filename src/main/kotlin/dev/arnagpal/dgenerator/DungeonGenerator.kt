package dev.arnagpal.dgenerator

import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.instance.buildInstance
import net.bladehunt.kotstom.dsl.instance.generator
import net.bladehunt.kotstom.dsl.instance.modify
import net.kyori.adventure.text.Component
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.monitoring.TickMonitor
import net.minestom.server.utils.MathUtils
import net.minestom.server.utils.chunk.ChunkSupplier
import net.minestom.server.utils.time.TimeUnit
import java.util.concurrent.atomic.AtomicReference


val GlobalEventNode: EventNode<Event> by lazy {
    EventNode.all("global")
}

val mainInstance: Instance by lazy {
    buildInstance {
        chunkSupplier = ChunkSupplier(::LightingChunk)
        generator {
            modify {
                fillHeight(0, 42, Block.STONE)
            }
        }
    }
}

fun main() {
    // Initialization
    val minecraftServer = MinecraftServer.init(Auth.Online())
    SchedulerManager.buildShutdownTask { println("Server closed") }

    // Set the global event node
    MinecraftServer.getGlobalEventHandler().addChild(GlobalEventNode)

    GlobalEventHandler.addListener(ServerTickMonitorEvent::class.java) { event: ServerTickMonitorEvent ->
        LAST_TICK.set(event.tickMonitor)
    }

    setupTickMonitor()

    // Start the server on port 25565
    minecraftServer.start("0.0.0.0", 25565)
}

private val LAST_TICK = AtomicReference<TickMonitor>()

private fun setupTickMonitor() {
    val benchmarkManager = MinecraftServer.getBenchmarkManager()
    MinecraftServer.getSchedulerManager().buildTask {
        if (LAST_TICK.get() == null || MinecraftServer.getConnectionManager()
                .onlinePlayerCount == 0
        ) return@buildTask
        var ramUsage = benchmarkManager.usedMemory
        ramUsage = (ramUsage / 1e6).toLong() // bytes to MB

        val tickMonitor: TickMonitor = LAST_TICK.get()
        val header: Component = Component.text("RAM USAGE: $ramUsage MB")
            .append(Component.newline())
            .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.tickTime, 2) + "ms"))
            .append(Component.newline())
            .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.acquisitionTime, 2) + "ms"))
        val footer: Component = benchmarkManager.getCpuMonitoringMessage()
        Audiences.players().sendPlayerListHeaderAndFooter(header, footer)
    }.repeat(10, TimeUnit.SERVER_TICK).schedule()
}
