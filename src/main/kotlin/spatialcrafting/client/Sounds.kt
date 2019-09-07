package spatialcrafting.client

import net.minecraft.sound.SoundEvent
import spatialcrafting.modId

object Sounds {
    val CraftEndId = modId("craft_end")
    val CraftLoopId = modId("craft_loop")
    val CraftStartId = modId("craft_start")
    val CraftEnd = SoundEvent(CraftEndId)
    val CraftLoop = SoundEvent(CraftLoopId)
    val CraftStart = SoundEvent(CraftStartId)

}

