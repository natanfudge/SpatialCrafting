@file:UseSerializers(ForBlockPos::class, ForIdentifier::class, ForItemStack::class, ForUuid::class, ForSoundEvent::class, ForVec3d::class)

package spatialcrafting

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.client.particle.ItemMovementParticle
import spatialcrafting.client.particle.centerOfHolograms
import spatialcrafting.client.particle.playAllCraftParticles
import spatialcrafting.crafter.*
import spatialcrafting.hologram.CraftingItemMovementData
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.recipe.CraftingEffect
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.*
import java.util.*

interface C2SPacket<T : Packet<T>> : InternalC2SPacket<T> {
    override val modId get() = ModId
}

interface S2CPacket<T : Packet<T>> : InternalS2CPacket<T> {
    override val modId get() = ModId
}

interface TwoSidedPacket<T : Packet<T>> : InternalTwoSidedPacket<T> {
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

    //TODO: the backup pos is probably not needed considering what is happening
    @Serializable
    data class UnassignMultiblockState(val anyCrafterPiecePos: BlockPos,
                                       /**
                                        * In case the first pos was a block that was destroyed in which case no multiblock can be gathered from it.
                                        */
                                       val backupCrafterPiecePos: BlockPos) : S2CPacket<UnassignMultiblockState> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val crafter = context.world.getBlockEntity(anyCrafterPiecePos) as? CrafterPieceEntity
                    ?: context.world.getCrafterEntity(backupCrafterPiecePos).also { logDebug { "Using backup crafterPiecePos" } }

            crafter.multiblockIn?.let {
                CrafterPieceEntity.unassignMultiblockState(context.world, it)
                it.cancellationTokens.craftingParticles?.cancel(context.world)
            }

            if (crafter.multiblockIn == null) logInfo { "No multiblock to unassign" }


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
    data class StartCraftingParticles(val anyCrafterPiecePos: BlockPos, private val duration: Long,
                                      private val effect: CraftingEffect) : S2CPacket<StartCraftingParticles> {
        override val serializer get() = serializer()


        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            if (effect == CraftingEffect.itemMovement) {
                for (hologram in multiblock.getHologramEntities(context.world)) {
                    hologram.craftingItemMovement = CraftingItemMovementData(
                            targetLocation = multiblock.centerOfHolograms(),
                            startTime = context.world.time,
                            endTime = context.world.time + duration
                    )
                }
            }
            else {
                playAllCraftParticles(context.world, multiblock, duration.ticks)
            }

        }
    }

    @Serializable
    data class StopCraftingParticles(val anyCrafterPiecePos: BlockPos) : S2CPacket<StopCraftingParticles> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            for (hologram in multiblock.getHologramEntities(context.world)) {
                hologram.craftingItemMovement = null
            }
            multiblock.cancellationTokens.craftingParticles?.cancel(context.world)
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
    data class StopRecipeHelp(val anyCrafterPiecePos: BlockPos) : TwoSidedPacket<StopRecipeHelp> {
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


