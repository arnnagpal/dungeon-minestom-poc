package dev.arnagpal.dgenerator.commands

import dev.arnagpal.dgenerator.dungeon.DungeonBuilder
import dev.arnagpal.dgenerator.dungeon.DungeonConfig
import dev.arnagpal.dgenerator.mainInstance
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import kotlin.math.min

class DungeonCommands {

    private val dungeonBuilder = DungeonBuilder()

    fun createDungeonCommand(): Command {
        return Command("dungeon").apply {

            val generateCommand = Command("generate").apply {
                val widthArg = ArgumentType.Integer("width").min(10).max(200)
                val heightArg = ArgumentType.Integer("height").min(10).max(200)
                val depthArg = ArgumentType.Integer("depth").min(3).max(50)
                val seedArg = ArgumentType.Long("seed")
                val roomsArg = ArgumentType.Integer("rooms").min(1).max(50)
                val minRoomSizeArg = ArgumentType.Integer("minRoomSize").min(3).max(20)
                val maxRoomSizeArg = ArgumentType.Integer("maxRoomSize").min(4).max(30)

                // /dungeon generate <width> <height> <depth>
                addSyntax({ sender, context ->
                    handleGenerate(
                        sender,
                        context.get(widthArg),
                        context.get(heightArg),
                        context.get(depthArg)
                    )
                }, widthArg, heightArg, depthArg)

                // /dungeon generate <width> <height> <depth> --seed <seed>
                addSyntax({ sender, context ->
                    handleGenerate(
                        sender,
                        context.get(widthArg),
                        context.get(heightArg),
                        context.get(depthArg),
                        seed = context.get(seedArg)
                    )
                }, widthArg, heightArg, depthArg, ArgumentType.Literal("--seed"), seedArg)

                // /dungeon generate <width> <height> <depth> --seed <seed> --rooms <maxRooms> --roomsize <minRoomSize> <maxRoomSize>
                addSyntax(
                    { sender, context ->
                        handleGenerate(
                            sender,
                            context.get(widthArg),
                            context.get(heightArg),
                            context.get(depthArg),
                            seed = context.get(seedArg),
                            maxRooms = context.get(roomsArg),
                            minRoomSize = context.get(minRoomSizeArg),
                            maxRoomSize = context.get(maxRoomSizeArg)
                        )
                    }, widthArg, heightArg, depthArg,
                    ArgumentType.Literal("--seed"), seedArg,
                    ArgumentType.Literal("--rooms"), roomsArg,
                    ArgumentType.Literal("--roomsize"), minRoomSizeArg, maxRoomSizeArg
                )
            }

            val areaCommand = Command("area").apply {
                val pos1Arg = ArgumentType.RelativeBlockPosition("pos1")
                val pos2Arg = ArgumentType.RelativeBlockPosition("pos2")
                val seedArg = ArgumentType.Long("seed")

                // /dungeon area <pos1> <pos2>
                addSyntax({ sender, context ->
                    handleArea(sender, context.get(pos1Arg).fromSender(sender), context.get(pos2Arg).fromSender(sender))
                }, pos1Arg, pos2Arg)

                // /dungeon area <pos1> <pos2> --seed <seed>
                addSyntax({ sender, context ->
                    handleArea(
                        sender,
                        context.get(pos1Arg).fromSender(sender),
                        context.get(pos2Arg).fromSender(sender),
                        seed = context.get(seedArg)
                    )
                }, pos1Arg, pos2Arg, ArgumentType.Literal("--seed"), seedArg)
            }

            val presetCommand = Command("preset").apply {
                val presetArg = ArgumentType.String("type")

                addSyntax({ sender, context ->
                    handlePreset(sender, context.get(presetArg))
                }, presetArg)

                setDefaultExecutor { sender, _ ->
                    sender.sendMessage(Component.text("Available presets:", NamedTextColor.YELLOW))
                    sender.sendMessage(Component.text("  small, medium, large, maze", NamedTextColor.WHITE))
                    sender.sendMessage(Component.text("Example: /dungeon preset medium", NamedTextColor.GRAY))
                }
            }

            val helpCommand = Command("help").apply {
                setDefaultExecutor { sender, _ ->
                    showHelp(sender)
                }
            }

            addSubcommand(generateCommand)
            addSubcommand(areaCommand)
            addSubcommand(presetCommand)
            addSubcommand(helpCommand)

            setDefaultExecutor { sender, _ ->
                showHelp(sender)
            }
        }
    }

    private fun handleGenerate(
        sender: net.minestom.server.command.CommandSender,
        width: Int,
        height: Int,
        depth: Int,
        seed: Long? = null,
        maxRooms: Int? = null,
        minRoomSize: Int? = null,
        maxRoomSize: Int? = null
    ) {
        val player = sender as Player
        val center = player.position
        val bounds = DungeonBuilder.DungeonBounds.fromCenterAndSize(center, width, height, depth)
        val config = createConfig(width, height, seed, maxRooms, minRoomSize, maxRoomSize)

        buildDungeonAsync(player, bounds, config)
    }

    private fun handleArea(
        sender: net.minestom.server.command.CommandSender,
        pos1: Point,
        pos2: Point,
        seed: Long? = null,
        maxRooms: Int? = null
    ) {
        val player = sender as Player
        val bounds = DungeonBuilder.DungeonBounds.fromCorners(pos1, pos2)
        val config = createConfig(bounds.width, bounds.height, seed, maxRooms)

        buildDungeonAsync(player, bounds, config)
    }

    private fun handlePreset(
        sender: net.minestom.server.command.CommandSender,
        presetName: String
    ) {
        val player = sender as Player
        val center = player.position

        val (bounds, config) = when (presetName.lowercase()) {
            "small" -> {
                val bounds = DungeonBuilder.DungeonBounds.fromCenterAndSize(center, 32, 32, 5)
                val config = DungeonConfig(
                    32,
                    32,
                    maxRooms = 8,
                    minRoomSize = 4,
                    maxRoomSize = 8,
                    seed = System.currentTimeMillis()
                )
                bounds to config
            }

            "medium" -> {
                val bounds = DungeonBuilder.DungeonBounds.fromCenterAndSize(center, 64, 64, 7)
                val config = DungeonConfig(
                    64,
                    64,
                    maxRooms = 15,
                    minRoomSize = 6,
                    maxRoomSize = 12,
                    seed = System.currentTimeMillis()
                )
                bounds to config
            }

            "large" -> {
                val bounds = DungeonBuilder.DungeonBounds.fromCenterAndSize(center, 96, 96, 9)
                val config = DungeonConfig(
                    96,
                    96,
                    maxRooms = 25,
                    minRoomSize = 8,
                    maxRoomSize = 16,
                    seed = System.currentTimeMillis()
                )
                bounds to config
            }

            "maze" -> {
                val bounds = DungeonBuilder.DungeonBounds.fromCenterAndSize(center, 80, 80, 6)
                val config = DungeonConfig(
                    80,
                    80,
                    maxRooms = 40,
                    minRoomSize = 3,
                    maxRoomSize = 7,
                    roomAttempts = 1000,
                    seed = System.currentTimeMillis()
                )
                bounds to config
            }

            else -> {
                player.sendMessage(Component.text("Unknown preset: $presetName", NamedTextColor.RED))
                player.sendMessage(Component.text("Available: small, medium, large, maze", NamedTextColor.GRAY))
                return
            }
        }

        buildDungeonAsync(player, bounds, config)
    }

    private fun buildDungeonAsync(
        player: Player,
        bounds: DungeonBuilder.DungeonBounds,
        config: DungeonConfig
    ) {
        player.sendMessage(Component.text("Generating dungeon using BSP...", NamedTextColor.YELLOW))
        player.sendMessage(
            Component.text(
                "Area: ${bounds.width}×${bounds.height}×${bounds.depth}",
                NamedTextColor.GRAY
            )
        )
        player.sendMessage(Component.text("Seed: ${config.seed}", NamedTextColor.GRAY))

        runBlocking {
            try {
                val result = dungeonBuilder.buildDungeon(mainInstance, bounds, config)

                if (result.success) {
                    player.sendMessage(Component.text("Dungeon generated successfully!", NamedTextColor.GREEN))
                    player.sendMessage(Component.text("Rooms: ${result.roomsGenerated}", NamedTextColor.AQUA))
                    player.sendMessage(Component.text("Corridors: ${result.corridorsGenerated}", NamedTextColor.AQUA))
                    player.sendMessage(Component.text("Blocks placed: ${result.blocksPlaced}", NamedTextColor.AQUA))
                    player.sendMessage(Component.text("Build time: ${result.buildTimeMs}ms", NamedTextColor.AQUA))
                } else {
                    player.sendMessage(
                        Component.text(
                            "Failed to generate dungeon: ${result.errorMessage}",
                            NamedTextColor.RED
                        )
                    )
                }
            } catch (e: Exception) {
                player.sendMessage(Component.text("Error generating dungeon: ${e.message}", NamedTextColor.RED))
                e.printStackTrace()
            }
        }
    }

    private fun createConfig(
        width: Int,
        height: Int,
        seed: Long? = null,
        maxRooms: Int? = null,
        minRoomSize: Int? = null,
        maxRoomSize: Int? = null
    ): DungeonConfig {
        return DungeonConfig(
            width = width,
            height = height,
            maxRooms = maxRooms ?: min(width * height / 50, 30),
            minRoomSize = minRoomSize ?: 4,
            maxRoomSize = maxRoomSize ?: min(width / 6, 15),
            seed = seed ?: System.currentTimeMillis(),
            roomAttempts = maxRooms?.let { it * 20 } ?: 500
        )
    }

    private fun showHelp(sender: net.minestom.server.command.CommandSender) {
        sender.sendMessage(Component.text("Basic Generation:", NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/dungeon generate <width> <height> <depth>", NamedTextColor.WHITE))
        sender.sendMessage(Component.text("/dungeon area <pos1> <pos2>", NamedTextColor.WHITE))
        sender.sendMessage(Component.text("/dungeon preset <small|medium|large|maze>", NamedTextColor.WHITE))
    }
}