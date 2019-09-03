package spatialcrafting.util

import drawer.readFrom
import drawer.write
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.world.World
import java.util.stream.Stream

/*******
 * Fabric Api Wrappers
 ********/

private fun CommonModInitializationContext.registerClientToServerPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) {
    ServerSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

private fun ClientModInitializationContext.registerServerToClientPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) {
    ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}


private fun <T : PlayerEntity> Stream<T>.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    for (player in this) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packetId, packet)
    }
}

private fun sendPacketToServer(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    ClientSidePacketRegistry.INSTANCE.sendToServer(packetId, packet)

}

/******************************
 * Automatic Serializer Wrappers
 ******************************/


fun <T : InternalC2SPacket<T>> CommonModInitializationContext.registerC2S(serializer: KSerializer<T>) {
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

fun <T : InternalS2CPacket<T>> ClientModInitializationContext.registerS2C(serializer: KSerializer<T>) {
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
fun <T : InternalS2CPacket<T>, U : PlayerEntity> Stream<U>.sendPacket(packet: T) {
    sendPacket(packetId = Identifier(packet.modId, packet.serializer.packetId)) { packet.serializer.write(packet, this) }
}

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : InternalC2SPacket<T>> sendPacketToServer(packet: T) {
    sendPacketToServer(Identifier(packet.modId,packet.serializer.packetId)){ packet.serializer.write(packet, this)}
}

val PacketContext.world: World get() = player.world



private val <T : Packet<T>> KSerializer<T>.packetId get() = descriptor.name.toLowerCase()

interface Packet<T : Packet<T>> {
    val serializer: KSerializer<T>
    val modId : String
    fun use(context: PacketContext)
}

interface InternalC2SPacket<T : Packet<T>> : Packet<T>
interface InternalS2CPacket<T : Packet<T>> : Packet<T>
interface InternalTwoSidedPacket<T : Packet<T>> : InternalS2CPacket<T>, InternalC2SPacket<T>

