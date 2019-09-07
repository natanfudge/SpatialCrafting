@file:UseSerializers(ForBlockPos::class, ForIdentifier::class)

package spatialcrafting.crafter

import drawer.ForBlockPos
import drawer.ForIdentifier
import drawer.put
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.IsHidden
import spatialcrafting.hologram.getHologramEntity
import spatialcrafting.hologram.isCloseEnoughToHologramPos
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.ticker.CancellationToken
import spatialcrafting.util.Duration
import spatialcrafting.util.ticks


const val RecipeCreatorCurrentLayerInactive = -1

@Serializable
data class CrafterMultiblockCancellationTokens(
        // Only exists in the server
        var craftingFinish: CancellationToken? = null,
        // Only exists in the client
        var craftingParticles: CancellationToken? = null
)

@Serializable
class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location (on the server, at least).
         */
        val crafterLocations: List<BlockPos> = listOf(),
        val multiblockSize: Int = 2,
        /**
         * For an ongoing craft
         */
        //TODO: remove
        private var _craftEndTime: Long? = null,
        /**
         * For recipe help, client only
         */
        var recipeHelpRecipeId: Identifier? = null,
        var recipeHelpCurrentLayer: Int = 0,
        /**
         * For recipe creator, client only
         */
        var recipeCreatorCurrentLayer: Int = RecipeCreatorCurrentLayerInactive,
        val cancellationTokens: CrafterMultiblockCancellationTokens = CrafterMultiblockCancellationTokens()
) {
    fun putIn(tag: CompoundTag) = serializer().put(this, tag)

    val craftEndTime: Duration?
        get() = _craftEndTime?.ticks


    fun setNotCrafting() {
        _craftEndTime = null
    }

    fun setIsCrafting(craftEndTime: Duration) {
        this._craftEndTime = craftEndTime.inTicks
    }

    val isCrafting: Boolean
        get() = _craftEndTime != null

    val recipeHelpActive: Boolean
        get() = recipeHelpRecipeId != null


    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = crafterLocations.map {
        world.getCrafterEntity(it)
    }

    fun getHologramEntities(world: World) = hologramLocations
            .map { world.getHologramEntity(it) }

    val hologramLocations: List<BlockPos> by lazy {
        this.crafterLocations.flatMap { pos ->
            (1..multiblockSize).map { pos.up(it) }
        }
    }

    fun arbitraryCrafterPos() = crafterLocations[0]
    fun differentArbitraryCrafterPos() = crafterLocations[1]

    fun canBeUsedByPlayer(player: PlayerEntity) = hologramLocations.all { player.isCloseEnoughToHologramPos(it) }

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

    fun setHologramVisibility(world: World, pos: BlockPos, hidden: Boolean) {
        world.setBlockState(pos, HologramBlock.defaultState.with(IsHidden, hidden))
    }

    fun hologramsRelativePositions(): List<HologramPos> {
        val originPosition = originHologramPos()
        return hologramLocations.map {
            HologramPos(it, it.relativeTo(originPosition))
        }
    }

    // (0,0,0) of holograms
    fun originHologramPos() = hologramLocations.minBy { it.x + it.y + it.z }!!

    fun BlockPos.relativeTo(originPos: BlockPos) = ComponentPosition(x - originPos.x, y - originPos.y, z - originPos.z)


    fun isLoadedAndHeightIsValid(world: World) = crafterLocations.all { world.isHeightValidAndBlockLoaded(it) }


    fun hologramsNotOfLayer(layer: Int): List<HologramPos> = hologramsRelativePositions()
            .filter { it.relativePos.y != layer }

    fun hologramsOfLayer(layer: Int): List<HologramPos> = hologramsRelativePositions()
            .filter { it.relativePos.y == layer }


    fun showHologramsWithItemOnly(world: World) {
        for (hologram in getHologramEntities(world)) {
            setHologramVisibility(world, hologram.pos, hidden = hologram.getItem().isEmpty)
        }
    }


    fun showHologramsOnlyOfLayer(layer: Int, world: World) {
        for (hologram in hologramsOfLayer(layer)) {
            setHologramVisibility(world, hologram.absolutePos, hidden = false)
        }

        for (hologram in hologramsNotOfLayer(layer)) {
            setHologramVisibility(world, hologram.absolutePos, hidden = true)
        }

    }


}


data class HologramPos(val absolutePos: BlockPos, val relativePos: ComponentPosition)