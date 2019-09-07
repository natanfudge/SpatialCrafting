@file:UseSerializers(ForIdentifier::class, ForUuid::class)

package spatialcrafting.ticker

import drawer.ForIdentifier
import drawer.ForUuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.server.world.ServerWorld
import spatialcrafting.C2SPacket
import spatialcrafting.S2CPacket
import spatialcrafting.util.*
import java.util.*

//TODO: register this in new mod env

internal fun CommonModInitializationContext.registerTickerC2SPackets() {
    registerC2S(TickInServerPacket.serializer(), TickerSerializersModule)
    registerC2S(CancelTickingInServerPacket.serializer())
}

internal fun ClientModInitializationContext.registerTickerC2SPackets() {
    registerS2C(FinishScheduleInClientPacket.serializer())
}


@Serializable
internal data class TickInServerPacket(val schedule: Schedule) : C2SPacket<TickInServerPacket> {
    override fun use(context: PacketContext) {
        if (context.world !is ServerWorld || context.world.isClient) {
            logWarning("A packet to the server is somehow not in a server world.")
            return
        }
        val scheduleable = getScheduleableFromRegistry(schedule.context.blockId) ?: return
        scheduleServer(context.world as ServerWorld, schedule, scheduleable)

    }

    @Transient
    override val serializer = serializer()
    @Transient
    override val serializationContext = TickerSerializersModule

}


@Serializable
internal data class FinishScheduleInClientPacket(val scheduleContext: ScheduleContext) : S2CPacket<FinishScheduleInClientPacket> {
    override fun use(context: PacketContext) {
        val scheduleable = getScheduleableFromRegistry(scheduleContext.blockId) ?: return
        scheduleable.onScheduleEnd(context.world, scheduleContext.blockPos, scheduleContext.scheduleId, scheduleContext.additionalData)
    }

    @Transient
    override val serializer = serializer()
}

@Serializable
internal data class CancelTickingInServerPacket(val cancellationUUID: UUID) : C2SPacket<CancelTickingInServerPacket> {
    override fun use(context: PacketContext) {
        if (context.world !is ServerWorld || context.world.isClient) {
            logWarning("A packet to the server is somehow not in a server world.")
            return
        }
        cancelScheduleServer(context.world as ServerWorld,cancellationUUID)
    }

    @Transient
    override val serializer = serializer()
}

