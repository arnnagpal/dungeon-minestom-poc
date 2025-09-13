package dev.arnagpal.dgenerator

import dev.arnagpal.dgenerator.commands.DungeonCommands
import dev.arnagpal.dgenerator.utils.debug.DebugMessage
import dev.arnagpal.dgenerator.utils.debug.shape.DebugShapeBox
import dev.arnagpal.dgenerator.utils.debug.shape.DebugShapeFlags
import net.bladehunt.kotstom.CommandManager
import net.bladehunt.kotstom.GlobalEventHandler
import net.bladehunt.kotstom.SchedulerManager
import net.bladehunt.kotstom.dsl.instance.buildInstance
import net.bladehunt.kotstom.dsl.instance.generator
import net.bladehunt.kotstom.dsl.instance.modify
import net.bladehunt.kotstom.dsl.kommand.defaultExecutor
import net.bladehunt.kotstom.dsl.kommand.kommand
import net.kyori.adventure.text.Component
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.builder.Command
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerLoadedEvent
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.event.trait.PlayerInstanceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.monitoring.TickMonitor
import net.minestom.server.utils.MathUtils
import net.minestom.server.utils.chunk.ChunkSupplier
import net.minestom.server.utils.time.TimeUnit
import team.unnamed.hephaestus.util.Quaternion
import java.util.concurrent.atomic.AtomicReference


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

    GlobalEventHandler.addListener(ServerTickMonitorEvent::class.java) { event: ServerTickMonitorEvent ->
        LAST_TICK.set(event.tickMonitor)
    }

    GlobalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event: AsyncPlayerConfigurationEvent ->
        val player = event.player
        // Set the player's view distance to 8 chunks
        event.spawningInstance = mainInstance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
    }

    GlobalEventHandler.addListener(PlayerLoadedEvent::class.java) { event: PlayerLoadedEvent ->
        val player = event.player
        player.gameMode = GameMode.CREATIVE
        player.isAllowFlying = true
        player.isFlying = true
    }

    setupTickMonitor()
    setupDungeonCommands()

    CommandManager.register(kommand("test") {
        defaultExecutor { sender ->
            DebugMessage.addShape(
                sender as Player,
                id = "test_box",
                shape = DebugShapeBox(
                    center = sender.position.add(0.0, 5.0, 0.0),
                    size = Pos(2.0, 2.0, 2.0),
                    rotation = Quaternion.IDENTITY,
                    faceArgb = 0x7F00FF00,
                    lineArgb = 0xFFFF0000.toInt(),
                    lineThickness = 1f
                ),
                flags = DebugShapeFlags.FLAG_SHOW_THROUGH_WALLS,
                lifetime = 200 // lasts for 200 ticks (10 seconds)
            )
        }
    } as Command)

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

private fun setupDungeonCommands() {
    CommandManager.register(DungeonCommands().createDungeonCommand())
}