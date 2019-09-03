@file:UseSerializers(ForBlockPos::class, ForIdentifier::class, ForItemStack::class, ForUuid::class)

package spatialcrafting

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.client.particle.ItemMovementParticle
import spatialcrafting.client.particle.playCraftParticles
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.assertIs
import spatialcrafting.crafter.getCrafterEntity
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.*
import java.util.*


interface C2SPacket<T : Packet<T>> : InternalC2SPacket<T>{
    override val modId get() = ModId
}
interface S2CPacket<T : Packet<T>> : InternalS2CPacket<T>{
    override val modId get() = ModId
}

interface TwoSidedPacket<T: Packet<T>> : InternalTwoSidedPacket<T>{
    override val modId get() = ModId
}



object Packets {


    @Serializable
    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityPos: BlockPos) : S2CPacket<AssignMultiblockState> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            CrafterPieceEntity.assignMultiblockState(context.world, masterEntityPos, multiblock)
        }

    }

    @Serializable
    data class UnassignMultiblockState(val anyCrafterPiecePos: BlockPos,
                                       /**
                                        * In case the first pos was a block that was destroyed in which case not multiblock can be gathered from it.
                                        */
                                       val backupCrafterPiecePos: BlockPos) : S2CPacket<UnassignMultiblockState> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val crafter = context.world.getBlockEntity(anyCrafterPiecePos) as? CrafterPieceEntity
                    ?: context.world.getCrafterEntity(backupCrafterPiecePos)

                CrafterPieceEntity.unassignMultiblockState(context.world, crafter.multiblockIn
                        ?: error("No multiblock to unassign"))

        }
    }

    @Serializable
    data class UpdateHologramContent(val hologramPos: BlockPos, val newItem: ItemStack) : S2CPacket<UpdateHologramContent> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val hologram = context.world.getBlockEntity(hologramPos).assertIs<HologramBlockEntity>(hologramPos)
            if (newItem.isEmpty) {
                hologram.extractItem()
            }
            else {
                if (hologram.isEmpty()) hologram.insertItem(newItem)
            }


            Client.scheduleRenderUpdate(hologramPos)

        }
    }


    @Serializable
    data class StartCraftingParticles(val multiblock: CrafterMultiblock, private val _duration: Long) : S2CPacket<StartCraftingParticles> {
        override val serializer get() = serializer()

        val duration: Duration get() = _duration.ticks

        /**workaround is used to make this constructor not clash with the primary constructor*/
        constructor(multiblock: CrafterMultiblock, duration: Duration, workaround: Byte = 0.toByte())
                : this(multiblock, duration.inTicks)


        override fun use(context: PacketContext) {
            // We do this so we can later change the state of the multiblock through one of the crafter entities,
            // so we can tell the client to cancel the particles.
            CrafterPieceEntity.assignMultiblockState(context.world,
                    anyCrafterPos = multiblock.arbitraryCrafterPos(),
                    multiblock = multiblock)

            playCraftParticles(context.world, multiblock, duration)
        }


    }

    @Serializable
    data class StartRecipeHelp(val anyCrafterPiecePos: BlockPos, val recipeId: Identifier) : C2SPacket<StartRecipeHelp> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            getAndValidateRecipe(recipeId, context.world) ?: return
            multiblock.startRecipeHelpServer(recipeId, context.world)
        }
    }


    @Serializable
    data class StopRecipeHelp(val anyCrafterPiecePos: BlockPos) : TwoSidedPacket<StopRecipeHelp>{
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            if (context.world.isClient) {
                multiblock.stopRecipeHelpCommon()
            }
            else {
                multiblock.stopRecipeHelpServer(context.world)
            }

        }
    }


    /**
     * - Checks the recipe matches with the player inventory + multiblock inventory
     * - Transfer every missing item of the recipe from the player inventory to the multiblock inventory
     * - Transfer every mismatching item in the multiblock to the player inventory
     */
    @Serializable
    data class AutoCraft(val anyCrafterPiecePos: BlockPos, val withInventoryOfPlayer: UUID, val recipeId: Identifier) : C2SPacket<AutoCraft> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            val player = context.world.getPlayerByUuid(withInventoryOfPlayer)
                    ?: logWarning { "AutoCraft initiated for unknown player with UUID $withInventoryOfPlayer." }.run { return }

            if (!multiblock.canBeUsedByPlayer(player)) {
                logWarning {
                    "AutoCraft initiated by player with UUID $withInventoryOfPlayer who cannot access the multiblock at ${multiblock.crafterLocations}"
                }
                return
            }

            val recipe = getAndValidateRecipe(recipeId, context.world) ?: return


            multiblock.autoCraft(context.world, player, recipe)
        }

    }

    @Serializable
    data class ChangeActiveLayer(val anyCrafterPiecePos: BlockPos, val toLayer: Int) : C2SPacket<ChangeActiveLayer> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            if (toLayer < 0 || toLayer >= multiblock.multiblockSize) {
                logWarning { "Attempt to change active layer to invalid layer '$toLayer'." }
                return
            }
            multiblock.showHologramsOnlyOfLayer(toLayer, context.world)
            multiblock.recipeCreatorCurrentLayer = toLayer

        }

    }

    @Serializable
    data class ItemMovementFromPlayerToMultiblockParticles(
            val player: UUID,
            val itemsFromPlayerToMultiblock: List<Pair<BlockPos, ItemStack>>,
            val itemsFromMultiblockToPlayer: List<Pair<BlockPos, ItemStack>>) : S2CPacket<ItemMovementFromPlayerToMultiblockParticles> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val player = context.world.getPlayerByUuid(player)
                    ?: error("ItemMovementFromPlayerToMultiblockParticles initiated for unknown player with UUID $player.")
            ItemMovementParticle.playItemMovementFromPlayerToMultiblock(player, itemsFromPlayerToMultiblock, itemsFromMultiblockToPlayer)

        }

    }


    private fun getAndValidateRecipe(recipeId: Identifier, world: World): SpatialRecipe? {
        val recipe = world.recipeManager.get(recipeId).orElse(null)
        if (recipe == null) {
            logWarning { "Attempt to use packet with non-existent recipe id '$recipe'! Packet will not apply." }
            return null
        }
        return recipe as SpatialRecipe
    }

    private fun getAndValidateMultiblock(anyCrafterPiecePos: BlockPos, world: World): CrafterMultiblock? {
        if (world.isHeightValidAndBlockLoaded(anyCrafterPiecePos)) {
            val multiblock = world.getCrafterEntity(anyCrafterPiecePos).multiblockIn!!
            if (multiblock.isLoadedAndHeightIsValid(world)) {
                return multiblock
            }
            else {
                logWarning {
                    "Attempt to use packet with unloaded multiblock '$multiblock'! Packet will not apply."
                }
            }
        }
        else {
            logWarning {
                "Attempt to use packet in unloaded position '$anyCrafterPiecePos'! Packet will not apply."
            }
        }
        return null
    }

}


