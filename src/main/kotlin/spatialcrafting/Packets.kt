@file:UseSerializers(ForBlockPos::class, ForIdentifier::class, ForItemStack::class, ForUuid::class)

package spatialcrafting

import drawer.*
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
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
import spatialcrafting.util.kotlinwrappers.ClientModInitializationContext
import spatialcrafting.util.kotlinwrappers.CommonModInitializationContext
import java.util.*
import java.util.stream.Stream


fun <T : C2SPacket<T>> CommonModInitializationContext.registerC2S(serializer: KSerializer<T>) {
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

fun <T : S2CPacket<T>> ClientModInitializationContext.registerS2C(serializer: KSerializer<T>) {
    registerServerToClientPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}


/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : S2CPacket<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {

    sendPacket(packetId = Identifier(ModId, packet.serializer.packetId)) { packet.serializer.write(packet, this) }
}


/**
 * Sends a packet from the server to the client for all the players in the stream.
 * @param packetBuilder Put the information you wish to send here
 */
private fun <T : PlayerEntity> Stream<T>.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    for (player in this) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packetId, packet)
    }
}


/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : C2SPacket<T>> sendPacketToServer(packet: T) {
    val buf = PacketByteBuf(Unpooled.buffer()).also { packet.serializer.write(packet, it) }
    ClientSidePacketRegistry.INSTANCE.sendToServer(modId(packet.serializer.packetId), buf)
}

private val <T : Packet<T>> KSerializer<T>.packetId get() = descriptor.name.toLowerCase()

interface Packet<T : Packet<T>> {
    val serializer: KSerializer<T>
    fun use(context: PacketContext)
}

interface C2SPacket<T : Packet<T>> : Packet<T>
interface S2CPacket<T : Packet<T>> : Packet<T>

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


            getMinecraftClient().scheduleRenderUpdate(hologramPos)

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
    data class StopRecipeHelp(val anyCrafterPiecePos: BlockPos) : C2SPacket<StopRecipeHelp>, S2CPacket<StopRecipeHelp> {
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

//    @Serializable
//    data class UpdateRecipeCreatorHologramLayer(val anyCrafterPiecePos: BlockPos,val layer : Int): C2SPacket<UpdateRecipeCreatorHologramLayer>{
//        override val serializer = serializer()
//        override fun use(context: PacketContext) {
//            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
//
//        }
//    }

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


