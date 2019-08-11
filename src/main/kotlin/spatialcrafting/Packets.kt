@file:UseSerializers(Serializers.ForBlockPos::class)

package spatialcrafting

import drawer.Serializers
import drawer.readFrom
import drawer.write
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.assertIs
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.util.kotlinwrappers.ModInitializationContext
import spatialcrafting.util.kotlinwrappers.world
import java.util.stream.Stream

fun <T : Packets.Packet<T>> ModInitializationContext.registerC2S(manager: Packets.PacketManager<T>) {
    registerClientToServerPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}

fun <T : Packets.Packet<T>> ModInitializationContext.registerS2C(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}

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
//
//inline fun <reified T : Packet<T>> Packet<T>.toJson() = Json(JsonConfiguration.Stable).stringify(manager.serializer, this as T)
//inline fun <reified T: Packet<T>> PacketManager<T>.fromJson(json : String) : T = Json(JsonConfiguration.Stable).parse(serializer, json)
//
//interface Packet<T : Packet<T>> {
//    val manager: PacketManager<T>
//}
//
//interface PacketManager<T : Packet<T>> {
//    val serializer: KSerializer<T>
//    fun use(packet: T)
//}
//
//fun main() {
//    val x = X(2)
//    val str = x.toJson()
//    val back = x.manager.fromJson(str)
//    assert(x == back)
//}
//
//@Serializable
//data class X(val y: Int) : Packet<X> {
//    override val manager get() = object : PacketManager<X> {
//        override val serializer = serializer()
//        override fun use(packet: X) {
//        }
//    }
//}


//    interface MyPacket<T : Packet<T>>: Packet<T> {
//      // Providing a KSerializer is simpler than overriding toJson
//       val serializer : KSerializer<T>
//        override fun toJson() : String =  Json.stringify(serializer,this )
//        override val manager: PacketManager<T>
//    }
//
//    interface MyPacketManager<T : Packet<T>> : PacketManager<T> {
//       val serializer : KSerializer<T>
//        override val id: String get() =  serializer.descriptor.name
//        override fun fromJson(json : String): T = Json.parse(serializer,json)
//        override fun use(packet: T)
//    }


//TODO: get ItemStack serializer working, then replace fromBuf, toBuf and Id with a serializer override (see above)
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

    @Serializable
    data class AssignMultiblockState(val multiblock: CrafterMultiblock, val masterEntityPos: BlockPos) : Packet<AssignMultiblockState> {
        override val manager: PacketManager<AssignMultiblockState>
            get() = AssignMultiblockState

        companion object : PacketManager<AssignMultiblockState> {
            override val id: String
                get() = "create_multiblock"

            override fun fromBuf(buf: PacketByteBuf): AssignMultiblockState = serializer().readFrom(buf)

            override fun use(context: PacketContext, packet: AssignMultiblockState) {
                CrafterPieceEntity.assignMultiblockState(context.world, packet.masterEntityPos, packet.multiblock)
            }

        }

        override fun addToByteBuf(buf: PacketByteBuf) = serializer().write(this, buf)
    }

    data class UnassignMultiblockState(val multiblock: CrafterMultiblock) : Packet<UnassignMultiblockState> {
        override val manager: PacketManager<UnassignMultiblockState>
            get() = UnassignMultiblockState

        companion object : PacketManager<UnassignMultiblockState> {
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
                return StartCraftingParticles(CrafterMultiblock.serializer().readFrom(buf), buf.readDuration())
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
            multiblock.writeTo(buf)
            buf.writeDuration(duration)
        }

    }

//    data class StartRecipeHelp(val multiblock: CrafterMultiblock, val identifier: Identifier)

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

