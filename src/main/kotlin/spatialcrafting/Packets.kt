@file:UseSerializers(Serializers.ForBlockPos::class, Serializers.ForIdentifier::class)

package spatialcrafting

import drawer.Serializers
import drawer.readFrom
import drawer.write
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
import spatialcrafting.client.Duration
import spatialcrafting.client.playCraftParticles
import spatialcrafting.client.readDuration
import spatialcrafting.client.writeDuration
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.assertIs
import spatialcrafting.crafter.getCrafterEntity
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.util.kotlinwrappers.ModInitializationContext
import spatialcrafting.util.kotlinwrappers.world
import spatialcrafting.util.logWarning
import java.util.stream.Stream

fun <T : Packets.OldPacket<T>> ModInitializationContext.registerOldC2S(manager: Packets.OldPacketManager<T>) {
    registerClientToServerPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}

fun <T : Packets.OldPacket<T>> ModInitializationContext.registerOldS2C(manager: Packets.OldPacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}

fun <T : Packets.Packet<T>> ModInitializationContext.registerC2S(serializer: KSerializer<T>) {
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

fun <T : Packets.Packet<T>> ModInitializationContext.registerS2C(serializer: KSerializer<T>) {
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
fun <T : Packets.OldPacket<T>, U : PlayerEntity> Stream<U>.sendOldPacket(packet: T) {
    sendPacket(packetId = Identifier(ModId, packet.manager.id), packetBuilder = { packet.addToByteBuf(this) })
}

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : Packets.Packet<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {
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
fun <T : Packets.Packet<T>> sendPacketToServer(packet: T) {
    val buf = PacketByteBuf(Unpooled.buffer()).also { packet.serializer.write(packet, it) }
    ClientSidePacketRegistry.INSTANCE.sendToServer(modId(packet.serializer.packetId), buf)
}

private val <T : Packets.Packet<T>> KSerializer<T>.packetId get() = descriptor.name.toLowerCase()


//TODO: get ItemStack serializer working, then replace fromBuf, toBuf and Id with a serializer override (see above)
object Packets {
    interface OldPacket<T : OldPacket<T>> {
        fun addToByteBuf(buf: PacketByteBuf)
        val manager: OldPacketManager<T>
    }


    interface OldPacketManager<T : OldPacket<T>> {
        val id: String
        fun fromBuf(buf: PacketByteBuf): T
        fun use(context: PacketContext, packet: T)
    }


    interface Packet<T : Packet<T>> {
        val serializer: KSerializer<T>
        fun use(context: PacketContext)
    }

    @Serializable
    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityPos: BlockPos) : Packet<AssignMultiblockState> {
        override fun use(context: PacketContext) {
            CrafterPieceEntity.assignMultiblockState(context.world, masterEntityPos, multiblock)
        }

        override val serializer: KSerializer<AssignMultiblockState>
            get() = serializer()
    }


    data class UnassignMultiblockState(val multiblock: CrafterMultiblock) : OldPacket<UnassignMultiblockState> {
        override val manager: OldPacketManager<UnassignMultiblockState>
            get() = UnassignMultiblockState

        companion object : OldPacketManager<UnassignMultiblockState> {
            override val id: String
                get() = "destroy_multiblock"

            override fun fromBuf(buf: PacketByteBuf): UnassignMultiblockState = UnassignMultiblockState(
                    CrafterMultiblock.serializer().readFrom(buf)
            )

            override fun use(context: PacketContext, packet: UnassignMultiblockState) {

                CrafterPieceEntity.unassignMultiblockState(context.world, packet.multiblock)


            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            multiblock.writeTo(buf)
        }
    }

    data class UpdateHologramContent(val hologramPos: BlockPos, val newItem: ItemStack) : OldPacket<UpdateHologramContent> {
        override val manager: OldPacketManager<UpdateHologramContent>
            get() = UpdateHologramContent

        companion object : OldPacketManager<UpdateHologramContent> {
            override fun fromBuf(buf: PacketByteBuf): UpdateHologramContent =
                    UpdateHologramContent(buf.readBlockPos(), buf.readItemStack())

            override fun use(context: PacketContext, packet: UpdateHologramContent) {
                val hologram = context.world.getBlockEntity(packet.hologramPos).assertIs<HologramBlockEntity>(packet.hologramPos)
                if (packet.newItem.isEmpty) {
                    hologram.extractItem()
                }
                else {
                    if (hologram.isEmpty()) hologram.insertItem(packet.newItem)
                }
            }

            override val id: String
                get() = "update_hologram_content"
        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeBlockPos(hologramPos)
            buf.writeItemStack(newItem)
        }
    }

    data class StartCraftingParticles(val multiblock: CrafterMultiblock, val duration: Duration) : OldPacket<StartCraftingParticles> {
        override val manager: OldPacketManager<StartCraftingParticles>
            get() = StartCraftingParticles

        companion object : OldPacketManager<StartCraftingParticles> {
            override fun fromBuf(buf: PacketByteBuf): StartCraftingParticles {
                return StartCraftingParticles(CrafterMultiblock.serializer().readFrom(buf), buf.readDuration())
            }

            override fun use(context: PacketContext, packet: StartCraftingParticles) = with(packet) {
                // We do this so we can later change the state of the multiblock through one of the crafter entities,
                // so we can tell the client to cancel the particles.
                CrafterPieceEntity.assignMultiblockState(context.world,
                        masterPos = multiblock.crafterLocations[0],
                        multiblock = multiblock)

                playCraftParticles(context.world, multiblock, duration)
            }


            override val id: String
                get() = "start_crafting_particles"
        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            multiblock.writeTo(buf)
            buf.writeDuration(duration)
        }

    }

    @Serializable
    data class StartRecipeHelp(val anyCrafterPiecePos: BlockPos, val recipeId: Identifier) : Packet<StartRecipeHelp> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
            multiblock.startRecipeHelp(recipeId, context.world)
        }
    }


    @Serializable
    data class StopRecipeHelp(val anyCrafterPiecePos: BlockPos) : Packet<StopRecipeHelp> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val multiblock = getAndValidateMultiblock(anyCrafterPiecePos, context.world) ?: return
           multiblock.stopRecipeHelp(context.world)

        }
    }

    private fun getAndValidateMultiblock(anyCrafterPiecePos: BlockPos, world: World): CrafterMultiblock? {
        if (world.isHeightValidAndBlockLoaded(anyCrafterPiecePos)) {
            val multiblock = world.getCrafterEntity(anyCrafterPiecePos).multiblockIn!!
            if (multiblock.isLoadedAndHeightIsValid(world)) {
                return multiblock
            }
            else {
                logWarning {
                    "Attempt to start recipe help in unloaded multiblock $multiblock! Recipe help will not apply."
                }
            }
        }
        else {
            logWarning {
                "Attempt to start recipe help in unloaded position $anyCrafterPiecePos! Recipe help will not apply."
            }
        }
        return null
    }

//    data class CancelCraftingParticles(val oneOfTheMultiblockCraftersPos: BlockPos) : Packet<CancelCraftingParticles> {
//        companion object : PacketManager<CancelCraftingParticles> {
//            override val id: String
//                get() = "cancel_crafting_particles"
//
//            override fun fromBuf(buf: PacketByteBuf): CancelCraftingParticles {
//                return CancelCraftingParticles(buf.readBlockPos())
//            }
//
//            override fun use(context: PacketContext, packet: CancelCraftingParticles) {
//                val multiblock = context.world.getCrafterEntity(packet.oneOfTheMultiblockCraftersPos).multiblockIn
//                if (multiblock == null) {
//                    logDebug {
//                        "Assuming the player left the game before the CancelCraftingParticles packet has reached him because" +
//                                "There is no multiblock data stored on his client for pos ${packet.oneOfTheMultiblockCraftersPos}."
//                    }
//                    return
//                }
//
//                multiblock.cancelCrafting(context.world)
//
//            }
//
//        }
//
//        override fun addToByteBuf(buf: PacketByteBuf) {
//            buf.writeBlockPos(oneOfTheMultiblockCraftersPos)
//        }
//
//        override val manager: PacketManager<CancelCraftingParticles>
//            get() = CancelCraftingParticles
//
//    }


}


