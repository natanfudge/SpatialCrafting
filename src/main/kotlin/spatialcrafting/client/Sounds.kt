package spatialcrafting.client

import net.minecraft.sound.SoundEvent
import spatialcrafting.id

object Sounds {
    val CraftEnd = SoundEvent(id("craft_end"))
    val CraftLoop = SoundEvent(id("craft_loop"))
    val CraftStart = SoundEvent(id("craft_start"))

    val CraftLoopDuration = 27.ticks
}