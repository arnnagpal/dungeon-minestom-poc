package dev.arnagpal.dgenerator.dungeon

data class DungeonConfig(
    val width: Int = 64,
    val height: Int = 64,
    val minRoomSize: Int = 4,
    val maxRoomSize: Int = 12,
    val maxRooms: Int = 15,
    val roomAttempts: Int = 500,
    val corridorWidth: Int = 1,
    val seed: Long = System.currentTimeMillis()
)