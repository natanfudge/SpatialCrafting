@file:UseSerializers(ForBlockPos::class, ForIdentifier::class, CancellationTokenSerializer::class)

package spatialcrafting.crafter

import drawer.ForBlockPos
import drawer.ForIdentifier
import drawer.put
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import scheduler.CancellationToken
import scheduler.CancellationTokenSerializer
import spatialcrafting.hologram.*
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.recipe.offsetBy


const val RecipeCreatorCurrentLayerInactive = -1

@Serializable
data class CrafterMultiblockCancellationTokens(
        // Only exists in the server
        var craftingFinish: CancellationToken? = null,
        // Only exists in the client
        var craftingParticles: CancellationToken? = null
)


//TODO: think about attaching the world to this somehow
@Serializable
class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location (on the server, at least).
         */
        val crafterLocations: List<BlockPos> = listOf(),
        val multiblockSize: Int = 2,
        /**
         * For recipe help, client only
         */
        var recipeHelpRecipeId: Identifier? = null,
        var recipeHelpCurrentLayer: Int = 0,
        /**
         * For recipe creator, client only
         */
        var recipeCreatorCurrentLayer: Int = RecipeCreatorCurrentLayerInactive,
        val cancellationTokens: CrafterMultiblockCancellationTokens = CrafterMultiblockCancellationTokens(),
         var filledHologramsCount: Int = 0
) {
    fun putIn(tag: CompoundTag) = serializer().put(this, tag)

//    fun recipeHelpRecipe(world)


    val isCrafting: Boolean
        get() = cancellationTokens.craftingFinish != null

    val recipeHelpActive: Boolean
        get() = recipeHelpRecipeId != null

    val totalHologramAmount = multiblockSize * multiblockSize * multiblockSize


    val hologramLocations: List<BlockPos> by lazy {
        this.crafterLocations.flatMap { pos ->
            (1..multiblockSize).map { pos.up(it) }
        }
    }

    val arbitraryCrafterPos get() = crafterLocations[0]
    val differentArbitraryCrafterPos get() = crafterLocations[1]

    // (0,0,0) of holograms
    val originHologramPos by lazy { hologramLocations.minBy { it.x + it.y + it.z }!! }
    private val originCrafterPiecePos by lazy { crafterLocations.minBy { it.x + it.z }!! }
    private val farthestCornerCrafterPiecePos by lazy { crafterLocations.maxBy { it.x + it.z }!! }
    val boundaries get() = MultiblockBoundaries(originCrafterPiecePos, farthestCornerCrafterPiecePos)

//    // Cache the mapping from hologram pos to ingredient so we can quickly get it when accepting items
//    @Transient private var helpRecipeFastAccessByPos : Map<BlockPos, Ingredient> = mapOf()
//    @Transient private var helpRecipeIdOnLastFastAccess : Identifier? = null
//
//    fun getHelpRecipeIngredientByPos(world: World, pos: BlockPos) : Ingredient?{
//        return recipeHelpRecipeId?.let{currentRecipeId->
//            if(helpRecipeIdOnLastFastAccess != currentRecipeId){
//
//                helpRecipeIdOnLastFastAccess = currentRecipeId
//            }
//            helpRecipeFastAccessByPos[pos]
//        }
//
//    }
//    @Transient private lateinit var

    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = crafterLocations.map {
        world.getCrafterEntity(it)
    }

    fun getHologramEntities(world: World) = hologramLocations
            .map { world.getHologramEntity(it) }


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

    fun getHologramByRelativePosition(world: World, position: ComponentPosition): HologramBlockEntity {
        return world.getHologramEntity(originHologramPos.offsetBy(position))
    }

    fun hologramsRelativePositions(): List<AbsoluteAndRelativePos> {
        val originPosition = originHologramPos
        return hologramLocations.map {
            AbsoluteAndRelativePos(it, it.relativeTo(originPosition))
        }
    }


    fun BlockPos.relativeTo(originPos: BlockPos) = ComponentPosition(x - originPos.x, y - originPos.y, z - originPos.z)


    fun isLoadedAndHeightIsValid(world: World) = crafterLocations.all { world.canSetBlock(it) }


    fun hologramsNotOfLayer(layer: Int): List<AbsoluteAndRelativePos> = hologramsRelativePositions()
            .filter { it.relativePos.y != layer }

    fun hologramsOfLayer(layer: Int): List<AbsoluteAndRelativePos> = hologramsRelativePositions()
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


data class AbsoluteAndRelativePos(val absolutePos: BlockPos, val relativePos: ComponentPosition)
data class MultiblockBoundaries(
        /**
         * The corner with the small x,z coordinates
         */
        val lowCorner: BlockPos,
        /**
         * The corner with the big x,z coordinates
         */
        val highCorner: BlockPos)
