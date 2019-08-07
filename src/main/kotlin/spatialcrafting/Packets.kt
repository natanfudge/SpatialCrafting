package spatialcrafting

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import spatialcrafting.client.Duration
import spatialcrafting.client.playCraftParticles
import spatialcrafting.client.readDuration
import spatialcrafting.client.writeDuration
import spatialcrafting.crafter.*
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.util.kotlinwrappers.world
import java.util.stream.Stream

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : Packets.Packet<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {
    sendPacket(packetId = Identifier(ModId, packet.manager.id), packetBuilder = { packet.addToByteBuf(this) })
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




object Packets {
    interface Packet<T : Packet<T>> {
        fun addToByteBuf(buf: PacketByteBuf)
        val manager: PacketManager<T>
    }

    interface PacketManager<T : Packet<T>> {
        val id: String
        fun fromBuf(buf: PacketByteBuf): T
        fun use(context: PacketContext, packet: T)
    }

    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityPos: BlockPos) : Packet<AssignMultiblockState> {
        override val manager: PacketManager<AssignMultiblockState>
            get() = AssignMultiblockState

        companion object : PacketManager<AssignMultiblockState> {
            override val id: String
                get() = "create_multiblock"

            override fun fromBuf(buf: PacketByteBuf): AssignMultiblockState = AssignMultiblockState(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!"),
                    buf.readBlockPos()
            )

            override fun use(context: PacketContext, packet: AssignMultiblockState) {
               CrafterPieceEntity.assignMultiblockState(context.world, packet.masterEntityPos, packet.multiblock)
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
            buf.writeBlockPos(masterEntityPos)
        }
    }

    data class UnassignMultiblockState(val multiblock: CrafterMultiblock) : Packet<UnassignMultiblockState> {
        override val manager: PacketManager<UnassignMultiblockState>
            get() = UnassignMultiblockState

        companion object : PacketManager<UnassignMultiblockState> {
            override val id: String
                get() = "destroy_multiblock"

            override fun fromBuf(buf: PacketByteBuf): UnassignMultiblockState = UnassignMultiblockState(
                    buf.readCompoundTag()?.toCrafterMultiblock()
                            ?: error("A compound was not written to the PacketByteBuf!")
            )

            override fun use(context: PacketContext, packet: UnassignMultiblockState) {
                CrafterPieceEntity.unassignMultiblockState(context.world, packet.multiblock)
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
        }
    }

    data class UpdateHologramContent(val hologramPos: BlockPos, val newItem: ItemStack) : Packet<UpdateHologramContent> {
        override val manager: PacketManager<UpdateHologramContent>
            get() = UpdateHologramContent

        companion object : PacketManager<UpdateHologramContent> {
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

    data class StartCraftingParticles(val multiblock: CrafterMultiblock, val duration: Duration) : Packet<StartCraftingParticles> {
        override val manager: PacketManager<StartCraftingParticles>
            get() = StartCraftingParticles

        companion object : PacketManager<StartCraftingParticles> {
            override fun fromBuf(buf: PacketByteBuf): StartCraftingParticles {
                return StartCraftingParticles(buf.readCompoundTag()!!.toCrafterMultiblock()!!, buf.readDuration())
            }

            override fun use(context: PacketContext, packet: StartCraftingParticles) {
                // We do this so we can later change the state of the multiblock through one of the crafter entities,
                // so we can tell the client to cancel the particles.
                CrafterPieceEntity.assignMultiblockState(context.world,
                        masterPos = packet.multiblock.crafterLocations[0],
                        multiblock = packet.multiblock)

                playCraftParticles(context.world, packet.multiblock, packet.duration)
            }


            override val id: String
                get() = "start_crafting_particles"
        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
            buf.writeDuration(duration)
        }

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

