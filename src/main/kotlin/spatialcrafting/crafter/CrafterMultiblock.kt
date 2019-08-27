@file:UseSerializers(ForBlockPos::class, ForIdentifier::class)

package spatialcrafting.crafter

import drawer.ForBlockPos
import drawer.ForIdentifier
import drawer.put
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.hologram.*
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.recipe.ShapedRecipeComponent
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.sendPacket
import spatialcrafting.util.*


private const val sizeKey = "size"
private const val locationKey = "location"
private const val craftEndTimeKey = "craft_end_time"

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
        private var _craftEndTime: Long? = null,
        /**
         * For recipe help, client only
         */
        var recipeHelpRecipeId: Identifier? = null,
        var recipeHelpCurrentLayer: Int = 0,
        /**
         * For recipe creator, client only
         */
        var recipeCreatorCurrentLayer : Int = 0
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

    private fun setHologramVisibility(world: World, pos: BlockPos, hidden: Boolean) {
        world.setBlockState(pos, HologramBlock.defaultState.with(IsHidden, hidden))
    }

    private fun hologramsRelativePositions(): List<HologramPos> {
        val originPosition = originHologramPos()
        return hologramLocations.map {
            HologramPos(it, it.relativeTo(originPosition))
        }
    }

    // (0,0,0) of holograms
    private fun originHologramPos() = hologramLocations.minBy { it.x + it.y + it.z }!!

    private fun BlockPos.relativeTo(originPos: BlockPos) = ComponentPosition(x - originPos.x, y - originPos.y, z - originPos.z)


    fun isLoadedAndHeightIsValid(world: World) = crafterLocations.all { world.isHeightValidAndBlockLoaded(it) }

    private fun helpRecipeComponents(world: World): List<ShapedRecipeComponent>? = recipeHelpRecipeId?.let {
        (world.recipeManager.get(it).orElse(null)!! as SpatialRecipe).previewComponents
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

    private fun hologramsNotOfLayer(layer: Int): List<HologramPos> = hologramsRelativePositions()
            .filter { it.relativePos.y != recipeHelpCurrentLayer }

    private fun hologramsOfLayer(layer: Int): List<HologramPos> = hologramsRelativePositions()
            .filter { it.relativePos.y == recipeHelpCurrentLayer }

    private fun hideAndShowHologramsForRecipeHelp(world: World) {
        val recipeInputs = helpRecipeComponents(world)!!
        for (hologram in hologramsNotOfLayer(recipeHelpCurrentLayer)) {
            setHologramVisibility(world, hologram.absolutePos, hidden = true)
        }


        for (hologram in hologramsOfLayer(recipeHelpCurrentLayer)) {
            val ingredient = recipeInputs.find { it.position == hologram.relativePos }
            if (ingredient != null) {
                setHologramVisibility(world, hologram.absolutePos, hidden = false)
            }
            else {
                setHologramVisibility(world, hologram.absolutePos, hidden = true)
            }
        }

    }

    fun showHologramsWithItemOnly(world: World) {
        for (hologram in getHologramEntities(world)) {
            setHologramVisibility(world, hologram.pos, hidden = hologram.getItem().isEmpty)
        }
    }

    private fun layerIsComplete(layer: Int, world: World): Boolean {
        val holograms = hologramsRelativePositions()
        return helpRecipeComponents(world)!!.filter { it.position.y == layer }
                .all { component ->
                    component.ingredient.matches(
                            world.getHologramEntity(holograms.first { it.relativePos == component.position }.absolutePos)
                                    .getItem()
                    )
                }
    }

//    fun decreaseRecipeHelpCurrentLayerIfNeeded(world: World) {
//        if ((0 until recipeHelpCurrentLayer).any { !layerIsComplete(it, world) }) {
//            assert(recipeHelpCurrentLayer <= 0)
//            logDebug { "Reducing recipe help layer" }
//            recipeHelpCurrentLayer--
//            hideAndShowHologramsForRecipeHelp(world)
//        }
//    }

    // Server only
    fun startRecipeHelpServer(recipeId: Identifier, world: World) {
        assert(world.isServer)
        startRecipeHelpCommon(recipeId)
        bumpRecipeHelpCurrentLayerIfNeeded(world)
        hideAndShowHologramsForRecipeHelp(world)
    }

    fun startRecipeHelpCommon(recipeId: Identifier) {
        recipeHelpRecipeId = recipeId
        recipeHelpCurrentLayer = 0
    }


    fun stopRecipeHelpServer(world: World) {
        assert(world.isServer)
        stopRecipeHelpCommon()
        showAllHolograms(world)
    }

    fun stopRecipeHelpCommon() {
        recipeHelpRecipeId = null
    }

    private fun showAllHolograms(world: World) {
        for (pos in hologramLocations) {
            setHologramVisibility(world, pos, hidden = false)
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

    fun hologramGhostIngredientFor(hologram: HologramBlockEntity): Ingredient? {
        val components = helpRecipeComponents(hologram.world!!) ?: return null
        val relativePos = hologram.pos.relativeTo(originHologramPos())
        return components.find { it.position == relativePos }?.ingredient
    }


    /**
     * - Checks the recipe matches with the player inventory + multiblock inventory
     * - Transfer every missing item of the recipe from the player inventory to the multiblock inventory
     * - Transfer every mismatching item in the multiblock to the player inventory
     */
    fun autoCraft(world: World, withInventoryOfPlayer: PlayerEntity, recipe: SpatialRecipe) {
        assert(world.isServer)
        val (satisfaction, isFullySatisfied) = getRecipeSatisfaction(
                recipe = recipe,
                nearestCrafter = this,
                world = world,
                player = withInventoryOfPlayer
        )
        if (!isFullySatisfied) {
            logWarning { "An autocraft was initiated when the player can't actually craft the recipe with his and the multiblock's inventory." }
            return
        }

        val relativeHologramPositions = hologramsRelativePositions()

        val particlesToSendFromMultiblockToPlayer = mutableListOf<Pair<BlockPos, ItemStack>>()
        val particlesToSendFromPlayerToMultiblock = mutableListOf<Pair<BlockPos, ItemStack>>()

        givePlayerMismatchingItems(world, withInventoryOfPlayer, satisfaction, particlesToSendFromMultiblockToPlayer)

        insertRecipeToMultiblock(satisfaction, world, relativeHologramPositions, particlesToSendFromPlayerToMultiblock)

        PlayerStream.watching(world, arbitraryCrafterPos()).sendPacket(Packets.ItemMovementFromPlayerToMultiblockParticles(
                withInventoryOfPlayer.uuid,
                itemsFromMultiblockToPlayer = particlesToSendFromMultiblockToPlayer,
                itemsFromPlayerToMultiblock = particlesToSendFromPlayerToMultiblock
        ))
    }

    fun arbitraryCrafterPos() = crafterLocations[0]
    fun differentArbitraryCrafterPos() = crafterLocations[1]

    private fun insertRecipeToMultiblock(satisfaction: List<ComponentSatisfaction>, world: World, relativeHologramPositions: List<HologramPos>, particlesToSendFromPlayerToMultiblock: MutableList<Pair<BlockPos, ItemStack>>) {
        for ((componentPos, satisfiedByStack, isAlreadyInMultiblock) in satisfaction) {
            if (!isAlreadyInMultiblock) {
                val hologram = world.getHologramEntity(relativeHologramPositions.first { it.relativePos == componentPos }.absolutePos)
                assert(hologram.isEmpty())
                particlesToSendFromPlayerToMultiblock.add(Pair(hologram.pos, satisfiedByStack!!))
                hologram.insertItem(satisfiedByStack.copy(1))
                satisfiedByStack.count--
            }
        }
    }

//    private fun ItemStack.move(amount : Int = this.count) = copy(amount).also { count -= amount }

    fun givePlayerMismatchingItems(world: World, player: PlayerEntity, satisfaction: List<ComponentSatisfaction>,
                                   particlesToSendFromMultiblockToPlayer: MutableList<Pair<BlockPos, ItemStack>>) {
        for (hologram in getHologramEntities(world)) {
            val item = hologram.getItem()
            if (satisfaction.none { it.satisfiedBy == item }) {
                particlesToSendFromMultiblockToPlayer.add(Pair(hologram.pos, item))
                player.offerOrDrop(hologram.extractItem())
            }
        }
    }


}


data class HologramPos(val absolutePos: BlockPos, val relativePos: ComponentPosition)