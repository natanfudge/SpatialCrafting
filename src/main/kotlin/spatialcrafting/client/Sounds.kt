package spatialcrafting.client

import net.minecraft.sound.SoundEvent
import spatialcrafting.modId
import spatialcrafting.util.ticks

object Sounds {
    const val CraftEndId = "craft_end"
    const val CraftLoopId = "craft_loop"
    const val CraftStartId = "craft_start"
    val CraftEnd = SoundEvent(modId(CraftEndId))
    val CraftLoop = SoundEvent(modId(CraftLoopId))
    val CraftStart = SoundEvent(modId(CraftStartId))

    val CraftLoopDuration = 27.ticks


}

