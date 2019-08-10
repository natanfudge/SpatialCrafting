@file:UseSerializers(Serializers.BlockPos::class, Serializers.Identifier::class)

package spatialcrafting.crafter

import drawer.Serializers
import drawer.put
import drawer.write
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.client.Duration
import spatialcrafting.client.ticks
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.recipe.ComponentPosition


private const val sizeKey = "size"
private const val locationKey = "location"
private const val craftEndTimeKey = "craft_end_time"

@Serializable
class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location (on the server, at least).
         */
        val crafterLocations: List<BlockPos>,
        val multiblockSize: Int,
        /**
         * For an ongoing craft
         */
        var _craftEndTime: Long? = null,
        /**
         * For recipe help
         */
        var recipeHelpRecipeId: Identifier? = null
) {
    fun putIn(tag: CompoundTag) = serializer().put(this, tag)
    fun writeTo(buf: PacketByteBuf) = serializer().write(this, buf)

    val craftEndTime: Duration?
        get() = _craftEndTime?.ticks


    fun setNotCrafting(world: World) {
        _craftEndTime = null
        world.getCrafterEntity(crafterLocations[0]).markDirty()
    }

    fun setIsCrafting(world: World, craftEndTime: Duration) {
        this._craftEndTime = craftEndTime.inTicks
        world.getCrafterEntity(crafterLocations[0]).markDirty()
    }

    val isCrafting: Boolean
        get() = _craftEndTime != null

    val recipeHelpActive: Boolean
        get() = recipeHelpRecipeId != null


    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = crafterLocations.map {
        world.getCrafterEntity(it)
    }

    fun getHologramEntities(world: World) = hologramLocations
            .map { world.getBlockEntity(it).assertIs<HologramBlockEntity>(it) }

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


