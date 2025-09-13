package dev.arnagpal.dgenerator.dungeon.structure


data class Room(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    val centerX: Int get() = x + width / 2
    val centerY: Int get() = y + height / 2

    fun intersects(other: Room, buffer: Int = 1): Boolean {
        return !(x - buffer >= other.x + other.width ||
                x + width + buffer <= other.x ||
                y - buffer >= other.y + other.height ||
                y + height + buffer <= other.y)
    }

    fun contains(px: Int, py: Int): Boolean {
        return px in x until (x + width) && py in y until (y + height)
    }
}