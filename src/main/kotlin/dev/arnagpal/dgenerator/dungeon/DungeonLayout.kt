package dev.arnagpal.dgenerator.dungeon

import dev.arnagpal.dgenerator.dungeon.structure.Corridor
import dev.arnagpal.dgenerator.dungeon.structure.Room


class DungeonLayout(
    val width: Int,
    val height: Int,
    val seed: Long
) {
    val rooms = mutableListOf<Room>()
    val corridors = mutableListOf<Corridor>()
    private val grid = Array(width) { Array(height) { DungeonTile.WALL } }

    fun setTile(x: Int, y: Int, tile: DungeonTile) {
        if (x in 0 until width && y in 0 until height) {
            grid[x][y] = tile
        }
    }

    fun getTile(x: Int, y: Int): DungeonTile {
        return if (x in 0 until width && y in 0 until height) {
            grid[x][y]
        } else {
            DungeonTile.WALL
        }
    }

    fun addRoom(room: Room) {
        rooms.add(room)
        // Carve out room
        for (x in room.x until room.x + room.width) {
            for (y in room.y until room.y + room.height) {
                setTile(x, y, DungeonTile.FLOOR)
            }
        }
    }

    fun addCorridor(corridor: Corridor) {
        corridors.add(corridor)
        corridor.points.forEach { (x, y) ->
            setTile(x, y, DungeonTile.CORRIDOR)
        }
    }
}