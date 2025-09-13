package dev.arnagpal.dgenerator.dungeon

import net.minestom.server.instance.block.Block

enum class DungeonTile(val block: Block) {
    WALL(Block.STONE_BRICKS),
    FLOOR(Block.STONE),
    CORRIDOR(Block.COBBLESTONE),
    AIR(Block.AIR)
}