package spatialcrafting.util.kotlinwrappers

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import java.util.stream.Stream

/**
 * Sends a packet from the server to the client for all the players in the stream.
 * @param packetBuilder Put the information you wish to send here
 */
fun <T : PlayerEntity >Stream<T>.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    for (player in this) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packetId, packet)
    }
}


