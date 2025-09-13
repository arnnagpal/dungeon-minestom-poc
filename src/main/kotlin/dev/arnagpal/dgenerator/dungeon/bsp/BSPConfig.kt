package dev.arnagpal.dgenerator.dungeon.bsp

import dev.arnagpal.dgenerator.dungeon.DungeonConfig


data class BSPConfig(
    val baseConfig: DungeonConfig,
    val maxSplitDepth: Int = 6,
    val minNodeSize: Int = 12,
    val splitBias: Float = 0.5f, // 0.0 = always horizontal, 1.0 = always vertical
    val roomPadding: Int = 1 // Minimum space between room edge and node edge
) {
    // Convenience properties
    val width: Int get() = baseConfig.width
    val height: Int get() = baseConfig.height
    val seed: Long get() = baseConfig.seed
    val minRoomSize: Int get() = baseConfig.minRoomSize
    val maxRoomSize: Int get() = baseConfig.maxRoomSize
}