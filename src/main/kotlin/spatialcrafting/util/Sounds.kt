@file:UseSerializers(ForSoundEvent::class, ForVec3d::class, ForIdentifier::class)

package spatialcrafting.util

import drawer.ForIdentifier
import drawer.ForSoundEvent
import drawer.ForVec3d
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.S2CPacket
import kotlin.math.max



@Serializable
data class PlaySoundPacket(val soundInstance: CommonPositionedSoundInstance)
    : S2CPacket<PlaySoundPacket> {
    override val serializer get() = serializer()
    override fun use(context: PacketContext) = context.world.play(soundInstance)
}

@Serializable
data class StopSoundPacket(val pos: Vec3d,val soundId: Identifier) : S2CPacket<StopSoundPacket> {
    override val serializer get() = serializer()
    override fun use(context: PacketContext) = context.world.stopSound(soundId,pos)
}

@Serializable
data class CommonPositionedSoundInstance(val soundEvent: SoundEvent,
                                         val category: SoundCategory,
                                         val pos: Vec3d,
                                         val volume: Float = 1.0f,
                                         val pitch: Float = 1.0f,
                                         val repeats: Boolean = false,
                                         val repeatDelay: Int = 0,
                                         val attenuationType: CommonAttenuationType = CommonAttenuationType.LINEAR,
                                         val relative: Boolean = false
) {
    internal fun toClientOnly() = ClientBuilders.soundInstance(
            soundEvent, category, pos, volume, pitch, repeats, repeatDelay,
            when (attenuationType) {
                CommonAttenuationType.NONE -> SoundInstance.AttenuationType.NONE
                CommonAttenuationType.LINEAR -> SoundInstance.AttenuationType.LINEAR
            },
            relative
    )

    internal fun sendToNearbyClients(world: World) = world.playersThatCanHear(pos, volume)
            .sendPacket(PlaySoundPacket(this))
}

private const val MaxSoundDistance = 16.0
private fun World.playersThatCanHear(pos: Vec3d, volume: Float) = PlayerStream.around(
        this, pos, max(MaxSoundDistance, MaxSoundDistance * volume)
)

enum class CommonAttenuationType {
    NONE,
    LINEAR;
}


//private val clientSounds = mutableMapOf<String, SoundInstance>()

///**
// * @param keyForStopping Only pass if you intend to stop
// */
fun World.play(soundInstance: CommonPositionedSoundInstance/*, keyForStopping: String? = null*/) {
    if (world.isClient) {
        val clientSound = soundInstance.toClientOnly()
        getMinecraftClient().soundManager.play(clientSound)
//        if (keyForStopping != null) clientSounds[keyForStopping] = clientSound
    }
    else soundInstance.sendToNearbyClients(world/*, keyForStopping*/)

}

//TODO: note, this system is pretty bad. It doesn't work for new people coming in, it doesn't stop when they leave the area,
// it might prevent one sound from stopping on time if sounds overlap, and more. make a lib.
/**
 * @param originalVolume Make sure to pass the same value as in [CommonPositionedSoundInstance] if that's customized
 */
fun World.stopSound(soundKey: Identifier,pos: Vec3d,  originalVolume: Float = 1.0f) {
    if (world.isClient) {
        //TODO: use stopSounds instead
        getMinecraftClient().soundManager.stopSounds(soundKey,null)
    }
    else playersThatCanHear(pos, originalVolume).sendPacket(StopSoundPacket(pos,soundKey))

}