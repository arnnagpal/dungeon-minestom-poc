package dev.arnagpal.dgenerator.dungeon.structure

data class Corridor(
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int,
    val points: List<Pair<Int, Int>>
)