@file:UseSerializers(ForBlockPos::class, ForIdentifier::class, ForItemStack::class)

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
import spatialcrafting.client.Duration
import spatialcrafting.client.playCraftParticles
import spatialcrafting.client.ticks
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.assertIs
import spatialcrafting.crafter.getCrafterEntity
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.util.kotlinwrappers.ClientModInitializationContext
import spatialcrafting.util.kotlinwrappers.CommonModInitializationContext
import spatialcrafting.util.kotlinwrappers.world
import spatialcrafting.util.logWarning
import java.util.stream.Stream

//fun <T : Packets.Packet<T>> CommonModInitializationContext.registerOldC2S(serializer: Packets.Packetserializer<T>) {
//    registerClientToServerPacket(serializer.id) { packetContext, packetByteBuf ->
//        serializer.use(packetContext, serializer.fromBuf(packetByteBuf))
//    }
//}
//
//fun <T : Packets.Packet<T>> ClientModInitializationContext.registerOldS2C(serializer: Packets.Packetserializer<T>) {
//    registerServerToClientPacket(serializer.id) { packetContext, packetByteBuf ->
//        serializer.use(packetContext, serializer.fromBuf(packetByteBuf))
//    }
//}

fun <T : Packets.Packet<T>> CommonModInitializationContext.registerC2S(serializer: KSerializer<T>) {
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

fun <T : Packets.Packet<T>> ClientModInitializationContext.registerS2C(serializer: KSerializer<T>) {
    registerServerToClientPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

///**
// * Sends a packet from the server to the client for all the players in the stream.
// */
//fun <T : Packets.Packet<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {
//    sendPacket(packetId = Identifier(ModId, packet.serializer.id), packetBuilder = { packet.addToByteBuf(this) })
//}

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
//    interface Packet<T : Packet<T>> {
//        fun addToByteBuf(buf: PacketByteBuf)
//        val serializer: Packetserializer<T>
//    }
//
//
//    interface Packetserializer<T : Packet<T>> {
//        val id: String
//        fun fromBuf(buf: PacketByteBuf): T
//        fun use(context: PacketContext, packet: T)
//    }


    interface Packet<T : Packet<T>> {
        val serializer: KSerializer<T>
        fun use(context: PacketContext)
    }

    @Serializable
    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityPos: BlockPos) : Packet<AssignMultiblockState> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            CrafterPieceEntity.assignMultiblockState(context.world, masterEntityPos, multiblock)
        }

    }


    @Serializable
    data class UnassignMultiblockState(val multiblock: CrafterMultiblock) : Packet<UnassignMultiblockState> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            CrafterPieceEntity.unassignMultiblockState(context.world, multiblock)
        }
    }

    @Serializable
    data class UpdateHologramContent(val hologramPos: BlockPos, val newItem: ItemStack) : Packet<UpdateHologramContent> {
        override val serializer get() = serializer()
        override fun use(context: PacketContext) {
            val hologram = context.world.getBlockEntity(hologramPos).assertIs<HologramBlockEntity>(hologramPos)
            if (newItem.isEmpty) {
                hologram.extractItem()
            }
            else {
                if (hologram.isEmpty()) hologram.insertItem(newItem)
            }
        }
    }

    @Serializable
    data class StartCraftingParticles(val multiblock: CrafterMultiblock, private val _duration: Long) : Packet<StartCraftingParticles> {
        override val serializer get() = serializer()

        val duration: Duration get() = _duration.ticks

        /**workaround is used to make this constructor not clash with the primary constructor*/
        constructor(multiblock: CrafterMultiblock, duration: Duration, workaround: Byte = 0.toByte())
                : this(multiblock, duration.inTicks)


        override fun use(context: PacketContext) {
            // We do this so we can later change the state of the multiblock through one of the crafter entities,
            // so we can tell the client to cancel the particles.
            CrafterPieceEntity.assignMultiblockState(context.world,
                    masterPos = multiblock.crafterLocations[0],
                    multiblock = multiblock)

            playCraftParticles(context.world, multiblock, duration)
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

}


