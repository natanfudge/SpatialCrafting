package spatialcrafting.crafter

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.client.Duration
import spatialcrafting.client.getDuration
import spatialcrafting.client.putDuration
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.util.Serializable
import spatialcrafting.util.kotlinwrappers.getBlockPos
import spatialcrafting.util.kotlinwrappers.putBlockPos
import spatialcrafting.util.kotlinwrappers.transformCompoundTag


private const val sizeKey = "size"
private const val locationKey = "location"
private const val craftEndTimeKey = "craft_end_time"

class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location (on the server, at least).
         */
        val crafterLocations: List<BlockPos>,
        val multiblockSize: Int,
        /**
         * For an ongoing craft
         */
        craftEndTime: Duration?
) : Serializable<CrafterMultiblock> {
    var craftEndTime: Duration? = null
        private set

    init {
        this.craftEndTime = craftEndTime
    }

    fun setNotCrafting(world: World) {
        craftEndTime = null
        world.getCrafterEntity(crafterLocations[0]).markDirty()
    }

    fun setIsCrafting(world: World, craftEndTime: Duration) {
        this.craftEndTime = craftEndTime
        world.getCrafterEntity(crafterLocations[0]).markDirty()
    }

    val isCrafting: Boolean
        get() = craftEndTime != null

    override fun toTag(): CompoundTag = CompoundTag().apply {
        crafterLocations.forEachIndexed { i, blockPos ->
            putBlockPos(locationKey + i, blockPos)
        }

        putInt(sizeKey, multiblockSize)
        if (craftEndTime != null) putDuration(craftEndTimeKey, craftEndTime!!)
    }

    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = crafterLocations.map {
        world.getCrafterEntity(it)
    }

    fun getHologramEntities(world: World) = hologramLocations.map { world.getBlockEntity(it) as HologramBlockEntity }

    val totalHologramAmount = multiblockSize * multiblockSize

    val hologramLocations: List<BlockPos>
        get() = this.crafterLocations.flatMap { pos ->
            (1..multiblockSize).map { pos.up(it) }
        }

    fun getInventory(world: World): CrafterMultiblockInventory {
        val entities = getHologramEntities(world).filter { !it.getItem().isEmpty }
        // The 'x' 'y' 'z' coordinates of a ComponentPosition are offset based, meaning they range from 0 to 4,
        // based on how big the multiblock is.
        // So we will try to get the '(0,0,0)' position to gain perspective, which will be the one with the lowest x,y,z.

        val originPos = entities.minBy { it.pos.x + it.pos.y + it.pos.z }?.pos
                ?: return CrafterMultiblockInventory(listOf())

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
    val craftEndTime = getDuration(craftEndTimeKey)

    val locations = (0 until totalPieceAmount(size)).mapNotNull { i ->
        getBlockPos(locationKey + i)
    }

    // If it's empty it means everything is null
    return if (locations.isEmpty()) return null
    else CrafterMultiblock(locations, size, craftEndTime)
}

/**
 * Gets the tag with the key and then deserializes it
 */
fun CompoundTag.addCrafterMultiblock(key: String): CrafterMultiblock? {
    return this.transformCompoundTag(key) { this.toCrafterMultiblock() }
}