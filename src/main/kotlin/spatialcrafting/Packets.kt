package spatialcrafting

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import spatialcrafting.util.sendMessage
import spatialcrafting.util.name
import spatialcrafting.util.sendPacket
import java.util.stream.Stream

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : Packets.Packet> Stream<ServerPlayerEntity>.sendPacket(packetManager: Packets.PacketManager<T>, packet: T) {
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

    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityLocation: BlockPos) : Packet {
        companion object : PacketManager<AssignMultiblockState> {
            override val id: String
                get() = "assign_multiblock_state"

            override fun fromBuf(buf: PacketByteBuf): AssignMultiblockState = AssignMultiblockState(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!"),
                    buf.readBlockPos()
            )

            override fun use(context: PacketContext, packet: AssignMultiblockState) {
                CrafterPieceEntity.assignMultiblockState(context.player.world, packet.masterEntityLocation, packet.multiblock)
                context.player.sendMessage("Multiblock!")
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
            buf.writeBlockPos(masterEntityLocation)
        }
    }

    data class UnassignMultiblockState(val multiblock: CrafterMultiblock) : Packet {
        companion object : PacketManager<UnassignMultiblockState> {
            override val id: String
                get() = "unassign_multiblock_state"

            override fun fromBuf(buf: PacketByteBuf): UnassignMultiblockState = UnassignMultiblockState(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!")
            )

            override fun use(context: PacketContext, packet: UnassignMultiblockState) {
                CrafterPieceEntity.unassignMultiblockState(context.player.world, packet.multiblock)
                context.player.sendMessage("No More multiblock!")
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
        }
    }




}

