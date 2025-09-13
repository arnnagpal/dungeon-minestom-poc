package dev.arnagpal.dgenerator.dungeon

import dev.arnagpal.dgenerator.dungeon.bsp.BSPGenerator
import dev.arnagpal.dgenerator.utils.debug.DebugMessage
import dev.arnagpal.dgenerator.utils.debug.shape.DebugShapeBox
import dev.arnagpal.dgenerator.utils.debug.shape.DebugShapeFlags
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import team.unnamed.hephaestus.util.Quaternion
import kotlin.math.max
import kotlin.math.min

class DungeonBuilder(private val algorithm: BSPGenerator = BSPGenerator()) {

    data class DungeonBounds(
        val minX: Int,
        val minY: Int,
        val minZ: Int,
        val maxX: Int,
        val maxY: Int,
        val maxZ: Int
    ) {
        val width: Int get() = maxX - minX + 1
        val height: Int get() = maxZ - minZ + 1
        val depth: Int get() = maxY - minY + 1

        companion object {
            fun fromCorners(corner1: Point, corner2: Point): DungeonBounds {
                return DungeonBounds(
                    minX = min(corner1.blockX(), corner2.blockX()),
                    minY = min(corner1.blockY(), corner2.blockY()),
                    minZ = min(corner1.blockZ(), corner2.blockZ()),
                    maxX = max(corner1.blockX(), corner2.blockX()),
                    maxY = max(corner1.blockY(), corner2.blockY()),
                    maxZ = max(corner1.blockZ(), corner2.blockZ())
                )
            }

            fun fromCenterAndSize(center: Point, width: Int, height: Int, depth: Int): DungeonBounds {
                val halfWidth = width / 2
                val halfHeight = height / 2
                val halfDepth = depth / 2

                return DungeonBounds(
                    minX = center.blockX() - halfWidth,
                    minY = center.blockY() - halfDepth,
                    minZ = center.blockZ() - halfHeight,
                    maxX = center.blockX() + halfWidth,
                    maxY = center.blockY() + halfDepth,
                    maxZ = center.blockZ() + halfHeight
                )
            }
        }
    }

    suspend fun buildDungeon(
        instance: Instance,
        bounds: DungeonBounds,
        config: DungeonConfig
    ): DungeonBuildResult {
        val startTime = System.currentTimeMillis()

        val adjustedConfig = config.copy(
            width = bounds.width,
            height = bounds.height
        )

        val layout = algorithm.generate(adjustedConfig)

        clearArea(instance, bounds)

        // show debug squares
        for (room in layout.rooms) {
            for (player in instance.players) {
                println("Room at (${bounds.minX + room.centerX - room.width / 2.0}, ${bounds.minY.toDouble()}, ${bounds.minZ + room.centerY - room.height / 2.0}) size (${room.width}x${room.height})")
                DebugMessage.addShape(
                    player,
                    "dgenerator",
                    "room_${room.centerX}_${room.centerY}",
                    DebugShapeBox(
                        Pos(
                            bounds.minX + room.centerX.toDouble(),
                            bounds.maxY.toDouble(),
                            bounds.minZ + room.centerY.toDouble()
                        ),
                        Pos(
                            room.width.toDouble(),
                            bounds.depth.toDouble(),
                            room.height.toDouble()
                        ),
                        Quaternion.IDENTITY,
                        0x7F00FF00,
                        0xFFFF0000.toInt(),
                        1.0f
                    ),
                    flags = DebugShapeFlags.FLAG_SHOW_THROUGH_WALLS
                )
            }
        }

        var blocksPlaced = 0
        for (x in 0 until bounds.width) {
            for (z in 0 until bounds.height) {
                val worldX = bounds.minX + x
                val worldZ = bounds.minZ + z

                val tile = layout.getTile(x, z)
                blocksPlaced += buildColumn(instance, worldX, worldZ, bounds, tile)
            }
        }

        val endTime = System.currentTimeMillis()

        return DungeonBuildResult(
            success = true,
            roomsGenerated = layout.rooms.size,
            corridorsGenerated = layout.corridors.size,
            blocksPlaced = blocksPlaced,
            buildTimeMs = endTime - startTime,
            bounds = bounds,
            seed = config.seed
        )
    }

    private fun clearArea(instance: Instance, bounds: DungeonBounds) {
        for (x in bounds.minX..bounds.maxX) {
            for (y in bounds.minY..bounds.maxY) {
                for (z in bounds.minZ..bounds.maxZ) {
                    instance.setBlock(x, y, z, Block.AIR)
                }
            }
        }
    }

    private fun buildColumn(
        instance: Instance,
        worldX: Int,
        worldZ: Int,
        bounds: DungeonBounds,
        tile: DungeonTile
    ): Int {
        var blocksPlaced = 0
        val floorY = bounds.minY
        val ceilingY = bounds.maxY
        val wallHeight = bounds.depth

        when (tile) {
            DungeonTile.WALL -> {
                for (y in floorY..ceilingY) {
//                    instance.setBlock(worldX, y, worldZ, tile.block) // commented to make walls invisible for debugging
                    blocksPlaced++
                }
            }

            DungeonTile.FLOOR -> {
                // floor
                instance.setBlock(worldX, floorY, worldZ, tile.block)
                blocksPlaced++

                // ceiling
                if (ceilingY > floorY) {
                    instance.setBlock(worldX, ceilingY, worldZ, Block.GLASS) // to see inside for debug
                    blocksPlaced++
                }

                // air in between
                for (y in floorY + 1 until ceilingY) {
                    instance.setBlock(worldX, y, worldZ, Block.AIR)
                }
            }

            DungeonTile.CORRIDOR -> {
                // floor (different block than rooms)
                instance.setBlock(worldX, floorY, worldZ, tile.block)
                blocksPlaced++

                // ceiling
                if (ceilingY > floorY) {
                    instance.setBlock(worldX, ceilingY, worldZ, Block.WHITE_STAINED_GLASS)
                    blocksPlaced++
                }

                // air in between
                for (y in floorY + 1 until ceilingY) {
                    instance.setBlock(worldX, y, worldZ, Block.AIR)
                }
            }

            DungeonTile.AIR -> {
                // just air (shouldn't happen normally)
                for (y in floorY..ceilingY) {
                    instance.setBlock(worldX, y, worldZ, Block.AIR)
                }
            }
        }

        return blocksPlaced
    }
}

data class DungeonBuildResult(
    val success: Boolean,
    val roomsGenerated: Int,
    val corridorsGenerated: Int,
    val blocksPlaced: Int,
    val buildTimeMs: Long,
    val bounds: DungeonBuilder.DungeonBounds,
    val seed: Long,
    val errorMessage: String? = null
)