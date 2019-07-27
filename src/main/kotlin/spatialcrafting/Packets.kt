package spatialcrafting

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import spatialcrafting.util.kotlinwrappers.sendPacket
import java.util.stream.Stream

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : Packets.Packet, U : PlayerEntity> Stream<U>.sendPacket(packetManager: Packets.PacketManager<T>, packet: T) {
    sendPacket(packetId = Identifier(ModId, packetManager.id), packetBuilder = { packet.addToByteBuf(this) })
}


object Packets {
    interface Packet {
        fun addToByteBuf(buf: PacketByteBuf)
    }

    interface PacketManager<T : Packet> {
        val id: String
        fun fromBuf(buf: PacketByteBuf): T
        fun use(context: PacketContext, packet: T)
    }

    data class CreateMultiblock(val multiblock: CrafterMultiblock, val masterEntityLocation: BlockPos) : Packet {
        companion object : PacketManager<CreateMultiblock> {
            override val id: String
                get() = "assign_multiblock_state"

            override fun fromBuf(buf: PacketByteBuf): CreateMultiblock = CreateMultiblock(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!"),
                    buf.readBlockPos()
            )

            override fun use(context: PacketContext, packet: CreateMultiblock) {
                CrafterPiece.createMultiblock(context.player.world, packet.masterEntityLocation, packet.multiblock)

            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
            buf.writeBlockPos(masterEntityLocation)
        }
    }

    data class DestroyMultiblock(val multiblock: CrafterMultiblock) : Packet {
        companion object : PacketManager<DestroyMultiblock> {
            override val id: String
                get() = "unassign_multiblock_state"

            override fun fromBuf(buf: PacketByteBuf): DestroyMultiblock = DestroyMultiblock(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!")
            )

            override fun use(context: PacketContext, packet: DestroyMultiblock) {
                CrafterPiece.destroyMultiblock(context.player.world, packet.multiblock)
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
        }
    }


}

