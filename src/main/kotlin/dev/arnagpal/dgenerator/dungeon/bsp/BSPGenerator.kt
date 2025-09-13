package dev.arnagpal.dgenerator.dungeon.bsp

import dev.arnagpal.dgenerator.dungeon.DungeonConfig
import dev.arnagpal.dgenerator.dungeon.DungeonLayout
import dev.arnagpal.dgenerator.dungeon.structure.Corridor
import dev.arnagpal.dgenerator.dungeon.structure.Room
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BSPGenerator {

    data class BSPNode(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        var left: BSPNode? = null,
        var right: BSPNode? = null,
        var room: Room? = null
    ) {
        val isLeaf: Boolean get() = left == null && right == null
        val centerX: Int get() = x + width / 2
        val centerY: Int get() = y + height / 2
    }

    fun generate(config: DungeonConfig): DungeonLayout {
        val random = Random(config.seed)
        val layout = DungeonLayout(config.width, config.height, config.seed)

        val rootNode = BSPNode(1, 1, config.width - 2, config.height - 2)

        splitNode(rootNode, config, random)

        generateRoomsInLeaves(rootNode, config, random)

        addRoomsToLayout(rootNode, layout)

        val corridors = connectRooms(rootNode, layout, random)
        corridors.forEach { layout.addCorridor(it) }

        return layout
    }

    private fun splitNode(node: BSPNode, config: DungeonConfig, random: Random): Boolean {
        // stop splitting if too small or we've reached depth
        if (node.width < config.minRoomSize * 3 || node.height < config.minRoomSize * 3) {
            return false
        }

        // prefer splitting along longer dimension
        val splitVertically = if (node.width > node.height * 1.25) {
            true
        } else if (node.height > node.width * 1.25) {
            false
        } else {
            random.nextBoolean()
        }

        if (splitVertically) {
            // vertical split
            val minSplitX = node.x + config.minRoomSize + 2
            val maxSplitX = node.x + node.width - config.minRoomSize - 2

            if (minSplitX >= maxSplitX) return false

            val splitX = random.nextInt(minSplitX, maxSplitX + 1)
            val leftWidth = splitX - node.x
            val rightWidth = node.width - leftWidth

            node.left = BSPNode(node.x, node.y, leftWidth, node.height)
            node.right = BSPNode(splitX, node.y, rightWidth, node.height)
        } else {
            // horizontal split
            val minSplitY = node.y + config.minRoomSize + 2
            val maxSplitY = node.y + node.height - config.minRoomSize - 2

            if (minSplitY >= maxSplitY) return false

            val splitY = random.nextInt(minSplitY, maxSplitY + 1)
            val topHeight = splitY - node.y
            val bottomHeight = node.height - topHeight

            node.left = BSPNode(node.x, node.y, node.width, topHeight)
            node.right = BSPNode(node.x, splitY, node.width, bottomHeight)
        }

        // recursively split children
        node.left?.let { splitNode(it, config, random) }
        node.right?.let { splitNode(it, config, random) }

        return true
    }

    private fun generateRoomsInLeaves(node: BSPNode, config: DungeonConfig, random: Random) {
        if (node.isLeaf) {
            val roomWidth = random.nextInt(
                config.minRoomSize,
                min(config.maxRoomSize, node.width - 2) + 1
            )
            val roomHeight = random.nextInt(
                config.minRoomSize,
                min(config.maxRoomSize, node.height - 2) + 1
            )

            val maxRoomX = node.x + node.width - roomWidth
            val maxRoomY = node.y + node.height - roomHeight

            val roomX = if (maxRoomX > node.x) {
                random.nextInt(node.x + 1, maxRoomX)
            } else {
                node.x + 1
            }

            val roomY = if (maxRoomY > node.y) {
                random.nextInt(node.y + 1, maxRoomY)
            } else {
                node.y + 1
            }

            node.room = Room(roomX, roomY, roomWidth, roomHeight)
        } else {
            // recursively generate rooms in children
            node.left?.let { generateRoomsInLeaves(it, config, random) }
            node.right?.let { generateRoomsInLeaves(it, config, random) }
        }
    }

    private fun addRoomsToLayout(node: BSPNode, layout: DungeonLayout) {
        if (node.isLeaf) {
            node.room?.let { layout.addRoom(it) }
        } else {
            node.left?.let { addRoomsToLayout(it, layout) }
            node.right?.let { addRoomsToLayout(it, layout) }
        }
    }

    private fun connectRooms(node: BSPNode, layout: DungeonLayout, random: Random): List<Corridor> {
        val corridors = mutableListOf<Corridor>()

        if (!node.isLeaf) {
            val leftRoom = getRandomRoom(node.left!!, random)
            val rightRoom = getRandomRoom(node.right!!, random)

            if (leftRoom != null && rightRoom != null) {
                val corridor = createBSPCorridor(leftRoom, rightRoom, random)
                corridors.add(corridor)
            }

            // recursively connect children
            node.left?.let { corridors.addAll(connectRooms(it, layout, random)) }
            node.right?.let { corridors.addAll(connectRooms(it, layout, random)) }
        }

        return corridors
    }

    private fun getRandomRoom(node: BSPNode, random: Random): Room? {
        if (node.isLeaf) {
            return node.room
        }

        val leftRoom = node.left?.let { getRandomRoom(it, random) }
        val rightRoom = node.right?.let { getRandomRoom(it, random) }

        return when {
            leftRoom != null && rightRoom != null -> if (random.nextBoolean()) leftRoom else rightRoom
            leftRoom != null -> leftRoom
            rightRoom != null -> rightRoom
            else -> null
        }
    }

    private fun createBSPCorridor(room1: Room, room2: Room, random: Random): Corridor {
        val startX = room1.centerX
        val startY = room1.centerY
        val endX = room2.centerX
        val endY = room2.centerY

        val points = mutableListOf<Pair<Int, Int>>()

        if (random.nextBoolean()) {
            val minX = min(startX, endX)
            val maxX = max(startX, endX)
            for (x in minX..maxX) {
                points.add(Pair(x, startY))
            }

            val minY = min(startY, endY)
            val maxY = max(startY, endY)
            for (y in minY..maxY) {
                points.add(Pair(endX, y))
            }
        } else {
            val minY = min(startY, endY)
            val maxY = max(startY, endY)
            for (y in minY..maxY) {
                points.add(Pair(startX, y))
            }

            val minX = min(startX, endX)
            val maxX = max(startX, endX)
            for (x in minX..maxX) {
                points.add(Pair(x, endY))
            }
        }

        return Corridor(startX, startY, endX, endY, points.distinct())
    }
}
