package spatialcrafting.crafter

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.util.Serializable
import spatialcrafting.util.kotlinwrappers.getBlockPos
import spatialcrafting.util.kotlinwrappers.putBlockPos
import spatialcrafting.util.kotlinwrappers.transformCompoundTag


private const val sizeKey = "size"
private const val locationKey = "location"

data class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location.
         */
        val locations: List<BlockPos>,
        val multiblockSize: Int
) : Serializable<CrafterMultiblock> {
    override fun toTag(): CompoundTag = CompoundTag().apply {
        locations.forEachIndexed { i, blockPos ->
            putBlockPos(locationKey + i, blockPos)
        }

        putInt(sizeKey, multiblockSize)
        val x = 2
    }

    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = locations.map {
        world.getCrafterEntity(it)
    }

    val hologramLocations: List<BlockPos>
        get() = this.locations.flatMap { pos ->
            (1..multiblockSize).map { pos.up(it) }
        }


}

fun totalPieceAmount(multiblockSize: Int) = multiblockSize * multiblockSize

fun CompoundTag.toCrafterMultiblock(): CrafterMultiblock? {
    val size = getInt(sizeKey)

    val locations = (0 until totalPieceAmount(size)).mapNotNull { i ->
        getBlockPos(locationKey + i)
    }

    // If it's empty it means everything is null
    return if (locations.isEmpty()) return null
    else CrafterMultiblock(locations, size)
}

/**
 * Gets the tag with the key and then deserializes it
 */
fun CompoundTag.toCrafterMultiblock(key: String): CrafterMultiblock? {
    return this.transformCompoundTag(key) { this.toCrafterMultiblock() }
}