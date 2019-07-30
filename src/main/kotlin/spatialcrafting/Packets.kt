package spatialcrafting

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import spatialcrafting.client.ParticleUtil
import spatialcrafting.client.seconds
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPiece
import spatialcrafting.crafter.toCrafterMultiblock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.util.kotlinwrappers.sendPacket
import spatialcrafting.util.kotlinwrappers.world
import java.util.stream.Stream

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : Packets.Packet<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {
    sendPacket(packetId = Identifier(ModId, packet.manager.id), packetBuilder = { packet.addToByteBuf(this) })
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

//    data class CreateMultiblock(val multiblock: CrafterMultiblock, val masterEntityLocation: BlockPos) : Packet {
//        companion object : PacketManager<CreateMultiblock> {
//            override val id: String
//                get() = "create_multiblock"
//
//            override fun fromBuf(buf: PacketByteBuf): CreateMultiblock = CreateMultiblock(
//                    buf.readCompoundTag()?.toCrafterMultiblock()
//                            ?: error("A compound was not written to the PacketByteBuf!"),
//                    buf.readBlockPos()
//            )
//
//            override fun use(context: PacketContext, packet: CreateMultiblock) {
//                CrafterPiece.createMultiblock(context.world, packet.masterEntityLocation, packet.multiblock)
//
//            }
//
//        }
//
//        override fun addToByteBuf(buf: PacketByteBuf) {
//            buf.writeCompoundTag(multiblock.toTag())
//            buf.writeBlockPos(masterEntityLocation)
//        }
//    }
//
//    data class DestroyMultiblock(val multiblock: CrafterMultiblock) : Packet {
//        companion object : PacketManager<DestroyMultiblock> {
//            override val id: String
//                get() = "destroy_multiblock"
//
//            override fun fromBuf(buf: PacketByteBuf): DestroyMultiblock = DestroyMultiblock(
//                    buf.readCompoundTag()?.toCrafterMultiblock()
//                            ?: error("A compound was not written to the PacketByteBuf!")
//            )
//
//            override fun use(context: PacketContext, packet: DestroyMultiblock) {
//                CrafterPiece.destroyMultiblock(context.world, packet.multiblock)
//            }
//
//        }
//
//        override fun addToByteBuf(buf: PacketByteBuf) {
//            buf.writeCompoundTag(multiblock.toTag())
//        }
//    }

    data class UpdateHologramContent(val hologramPos: BlockPos, val newItem: ItemStack) : Packet<UpdateHologramContent> {
        override val manager: PacketManager<UpdateHologramContent>
            get() = UpdateHologramContent

        companion object : PacketManager<UpdateHologramContent> {
            override fun fromBuf(buf: PacketByteBuf): UpdateHologramContent =
                    UpdateHologramContent(buf.readBlockPos(), buf.readItemStack())

            override fun use(context: PacketContext, packet: UpdateHologramContent) {
                val hologram = context.world.getBlockEntity(packet.hologramPos) as HologramBlockEntity
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

    data class StartCraftingParticles(val multiblock: CrafterMultiblock) : Packet<StartCraftingParticles> {
        override val manager: PacketManager<StartCraftingParticles>
            get() = StartCraftingParticles

        companion object : PacketManager<StartCraftingParticles> {
            override fun fromBuf(buf: PacketByteBuf): StartCraftingParticles {
                return StartCraftingParticles(buf.readCompoundTag()!!.toCrafterMultiblock()!!)
            }

            override fun use(context: PacketContext, packet: StartCraftingParticles) {
                ParticleUtil.playCraftParticles(context.world, packet.multiblock, 10.seconds)
            }


            override val id: String
                get() = "start_crafting_particles"
        }

        override fun addToByteBuf(buf: PacketByteBuf) {
            buf.writeCompoundTag(multiblock.toTag())
        }

    }


}

