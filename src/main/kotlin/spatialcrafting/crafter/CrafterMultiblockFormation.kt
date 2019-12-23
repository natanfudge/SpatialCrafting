package spatialcrafting.crafter

import fabricktx.api.*
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.AirBlock
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.logDebug

 fun CrafterPieceBlock.attemptToFormMultiblock(world: World, blockPos: BlockPos, byPlayer: LivingEntity?) {
    assert(world.isServer)
    val northernEasternCrafter = getNorthernEasternCrafter(world, blockPos)
    val multiblock = findPossibleMultiblock(world, northernEasternCrafter) ?: return

    if (thereIsSpaceForHolograms(world, multiblock)) {
        createMultiblockFromServer(world, northernEasternCrafter, multiblock)
    }
    else {
        byPlayer?.sendMessage(TranslatableText("message.spatialcrafting.no_space"))
    }


}

private fun createMultiblockFromServer(world: World, northernEasternCrafter: BlockPos, multiblock: CrafterMultiblock) {
    assert(world.isServer)
    createMultiblock(
            world = world,
            masterPos = northernEasternCrafter,
            multiblock = multiblock
    )
    PlayerStream.watching(world, multiblock.arbitraryCrafterPos).sendPacket(Packets.AssignMultiblockState(
            multiblock = multiblock,
            masterEntityPos = northernEasternCrafter
    ))

}
// Gets all pairs of the form (0,0), (0,1) ... (0,size -1) ... (1,0) ...
private fun pairsUntil(num : Int) : List<Pair<Int,Int>>{
    val list = mutableListOf<Pair<Int,Int>>()
    for(i in 0 until num){
        for(j in 0 until num){
            list.add(Pair(i,j))
        }
    }
    return list
}

private fun CrafterPieceBlock.findPossibleMultiblock(world: World, northernEasternCrafterPos: BlockPos): CrafterMultiblock? {
    val blocks = mutableListOf<BlockPos>()
    for ((westDistance, southDistance) in pairsUntil(size)) {
        val location = northernEasternCrafterPos.west(westDistance).south(southDistance)
        if (!thereIsACompatibleCrafterPieceIn(world, location)) {
            logDebug { "Refusing to build multiblock due to a '${world.getBlock(location)}' existing in a position that needs to hold a crafter piece: ${location.xz}" }
            return null // All nearby blocks must be crafter pieces
        }
        blocks.add(location)

    }

    return CrafterMultiblock(blocks, size)
}


private fun CrafterPieceBlock.getNorthernEasternCrafter(world: World, blockPos: BlockPos): BlockPos {
    var currentBlock = blockPos
    // Only go as far as size - 1 blocks away
    repeat(size - 1) {
        val northernBlock = currentBlock.north()
        val easternBlock = currentBlock.east()
        val northernEasternBlock = currentBlock.north().east()

        when {
            thereIsACompatibleCrafterPieceIn(world, northernEasternBlock) -> currentBlock = northernEasternBlock
            thereIsACompatibleCrafterPieceIn(world, northernBlock) -> currentBlock = northernBlock
            thereIsACompatibleCrafterPieceIn(world, easternBlock) -> currentBlock = easternBlock
        }


    }
    return currentBlock
}

private fun CrafterPieceBlock.thereIsACompatibleCrafterPieceIn(world: World, blockPos: BlockPos) = world.getBlock(blockPos).let {
    it is CrafterPieceBlock && it.size == this.size
}

private fun thereIsSpaceForHolograms(world: World, multiblock: CrafterMultiblock): Boolean =
        multiblock.hologramLocations.all {
            val isAir = world.getBlock(it) is AirBlock
            if (isAir) true
            else {
                if (world.isServer) logDebug { "Refusing to create multiblock due to a '${world.getBlock(it)}' blocking the space for a hologram in $it." }
                false
            }
        }


private fun CrafterMultiblock.logString() = "Size = $multiblockSize, Locations = \n" + crafterLocations.groupBy { it.x }
        .entries
        .joinToString("\n") { column -> column.value.joinToString(", ") { it.xz } }

fun createMultiblock(world: World, masterPos: BlockPos, multiblock: CrafterMultiblock) {
    assert(world.isServer)

    logDebug { "Building multiblock. [${multiblock.logString()}]" }

//    for (hologramPos in multiblock.hologramLocations) {
//        world.setBlock(HologramBlock, pos = hologramPos)
//    }

    CrafterPieceEntity.assignMultiblockState(world, masterPos, multiblock)

}

fun destroyMultiblock(world: World, multiblock: CrafterMultiblock) {
    assert(world.isServer)
    logDebug { "Destroying multiblock.\n [${multiblock.logString()}]" }

    CrafterPieceEntity.unassignMultiblockState(world, multiblock)

    for (hologramPos in multiblock.hologramLocations) {
        world.setBlock(Blocks.AIR, pos = hologramPos)
        world.removeBlockEntity(hologramPos)
    }

}

fun destroyMultiblockFromServer(world: World, multiblock: CrafterMultiblock) {
    assert(world.isServer)
    destroyMultiblock(world, multiblock)
    PlayerStream.watching(world, multiblock.arbitraryCrafterPos)
            .sendPacket(Packets.UnassignMultiblockState(
                    multiblock.arbitraryCrafterPos,
                    multiblock.differentArbitraryCrafterPos
            ))
}

