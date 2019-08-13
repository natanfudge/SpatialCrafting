package spatialcrafting.client

import net.minecraft.sound.SoundEvent
import spatialcrafting.modId

object Sounds {
    val CraftEnd = SoundEvent(modId("craft_end"))
    val CraftLoop = SoundEvent(modId("craft_loop"))
    val CraftStart = SoundEvent(modId("craft_start"))

    val CraftLoopDuration = 27.ticks
}