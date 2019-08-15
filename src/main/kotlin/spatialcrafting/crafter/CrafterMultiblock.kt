@file:UseSerializers(ForBlockPos::class, ForIdentifier::class)

package spatialcrafting.crafter

import drawer.ForBlockPos
import drawer.ForIdentifier
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
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.IsHidden
import spatialcrafting.hologram.getHologramEntity
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.logDebug
import spatialcrafting.util.matches


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
        private var _craftEndTime: Long? = null,
        /**
         * For recipe help, client only
         */
        var recipeHelpRecipeId: Identifier? = null,
        var recipeHelpCurrentLayer: Int = 0
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
            .map { world.getHologramEntity(it) }

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

    private fun setHologramVisibility(world: World, pos: BlockPos, hidden: Boolean) {
        world.setBlockState(pos, HologramBlock.defaultState.with(IsHidden, hidden))
    }


    fun startRecipeHelp(recipeId: Identifier, world: World) {
        recipeHelpRecipeId = recipeId
        recipeHelpCurrentLayer = 0
        bumpRecipeHelpCurrentLayerIfNeeded(world)
        hideAndShowHologramsForRecipeHelp(world)
    }

    fun stopRecipeHelp(world: World) {
        recipeHelpRecipeId = null
        showAllHolograms(world)
    }

    fun bumpRecipeHelpCurrentLayerIfNeeded(world: World) {
        if (layerIsComplete(recipeHelpCurrentLayer, world)) {
            if (recipeHelpCurrentLayer < multiblockSize - 1) {
                logDebug { "Bumping recipe help layer" }
                recipeHelpCurrentLayer++
                hideAndShowHologramsForRecipeHelp(world)
                bumpRecipeHelpCurrentLayerIfNeeded(world)
            }
            else {
                showHologramsWithItemOnly(world)
                logDebug { "Recipe help done" }
            }

        }
    }

    fun decreaseRecipeHelpCurrentLayerIfNeeded(world: World) {
        if ((0 until recipeHelpCurrentLayer).any { !layerIsComplete(it, world) }) {
            assert(recipeHelpCurrentLayer <= 0)
            logDebug { "Reducing recipe help layer" }
            recipeHelpCurrentLayer--
            hideAndShowHologramsForRecipeHelp(world)
        }
    }

    fun showHologramsWithItemOnly(world: World) {
        for (hologram in getHologramEntities(world)) {
            setHologramVisibility(world, hologram.pos, hidden = hologram.getItem().isEmpty)
        }
    }

    fun showAllHolograms(world: World) {
        for (pos in hologramLocations) {
            setHologramVisibility(world, pos, hidden = false)
        }
    }

    fun hideAndShowHologramsForRecipeHelp(world: World) {
        val recipeInputs = helpRecipeComponents(world)
        for (hologram in hologramsRelativeLocations().filter { it.relativePos.y != recipeHelpCurrentLayer }) {
            setHologramVisibility(world, hologram.absolutePos, hidden = true)
        }


        for (hologram in hologramsRelativeLocations().filter { it.relativePos.y == recipeHelpCurrentLayer }) {
            val ingredient = recipeInputs.find { it.position == hologram.relativePos }
            if (ingredient != null) {
                //TODO: set ghost item
                setHologramVisibility(world, hologram.absolutePos, hidden = false)
            }
            else {
                setHologramVisibility(world, hologram.absolutePos, hidden = true)
            }
        }

    }

    private fun helpRecipeComponents(world: World) =
            (world.recipeManager.get(recipeHelpRecipeId).orElse(null)!! as SpatialRecipe).previewComponents

    private fun layerIsComplete(layer: Int, world: World): Boolean {
        val holograms = hologramsRelativeLocations()
        return helpRecipeComponents(world).filter { it.position.y == layer }
                .all { component ->
                    component.ingredient.matches(
                            world.getHologramEntity(holograms.first { it.relativePos == component.position }.absolutePos)
                                    .getItem()
                    )
                }
    }


    private fun hologramsRelativeLocations(): List<HologramPos> {
        // (0,0,0) of holograms
        val locations = hologramLocations
        val originPosition = locations.minBy { it.x + it.y + it.z }!!
        return locations.map {
            HologramPos(it,
                    ComponentPosition(it.x - originPosition.x, it.y - originPosition.y, it.z - originPosition.z)
            )
        }
    }

    fun isLoadedAndHeightIsValid(world: World) = crafterLocations.all { world.isHeightValidAndBlockLoaded(it) }


}


data class HologramPos(val absolutePos: BlockPos, val relativePos: ComponentPosition)