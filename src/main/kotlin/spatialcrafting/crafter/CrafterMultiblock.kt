package spatialcrafting.crafter

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.recipe.ComponentPosition
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

    fun getHologramEntities(world: World) = hologramLocations.map { world.getBlockEntity(it) as HologramBlockEntity }

    val totalHologramAmount = multiblockSize * multiblockSize

    val hologramLocations: List<BlockPos>
        get() = this.locations.flatMap { pos ->
            (1..multiblockSize).map { pos.up(it) }
        }

    fun getInventory(world: World): CrafterMultiblockInventory {
        val entities = getHologramEntities(world).filter { !it.getItem().isEmpty }
        // The 'x' 'y' 'z' coordinates of a ComponentPosition are offset based, meaning they range from 0 to 4,
        // based on how big the multiblock is.
        // So we will try to get the '(0,0,0)' position to gain perspective, which will be the one with the lowest x,y,z.

        val originPos = entities.minBy { it.pos.x + it.pos.y + it.pos.z }?.pos ?: return CrafterMultiblockInventory(listOf())

        val components = entities.map {
            CrafterMultiblockInventorySlot(
                    position = ComponentPosition(x = it.pos.x - originPos.x, y = it.pos.y - originPos.y, z = it.pos.z - originPos.z),
                    itemStack = it.getItem()
            )
        }.sortedByXYZ()

        return CrafterMultiblockInventory(components)
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