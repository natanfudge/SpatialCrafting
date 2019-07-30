package spatialcrafting.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.client.CraftParticle.Companion.PHASE_2_START
import spatialcrafting.client.CraftParticle.Companion.PHASE_2_Y_END_POS_INCREASE_PER_TICK
import spatialcrafting.client.CraftParticle.Companion.SLAMDOWN_SPEED_BLOCKS_PER_TICK
import spatialcrafting.client.CraftParticle.Companion.SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK
import spatialcrafting.client.CraftParticle.Companion.SPEED_BLOCKS_PER_TICK_BASE
import spatialcrafting.crafter.CrafterMultiblock
import kotlin.math.roundToInt


object ParticleUtil {
    //    private val TICKS_BETWEEN_PARTICLES = (0.1f * TICKS_PER_SECOND).toInt()
    private val TICKER_ID = "ticker_particle_item_dust"

    private fun ParticleUtil() {}

    fun playCraftParticles(world: World, multiblock: CrafterMultiblock, duration: Duration) {
        //TODO: call multiple times

        val timePassed = Duration(0)
        GlobalScope.launch {
            for (hologram in multiblock.getHologramEntities(world)) {
                val itemStack: ItemStack = hologram.getItem()

                val hologramPos = hologram.pos.toVec3d()


                if (itemStack.item != Items.AIR) {

                    // Calculate start and end positions
                    val startPos = hologramPos + Vec3d(0.2, 0.5, 0.2)
                    val currentEndPos: Vec3d = multiblock.centerOfHolograms()
                    val crafters = multiblock.locations
                    val craftYEndPos: Double = crafters[0].y + 1.5
                    if (duration > getRelativeTicksPassed(timePassed, duration, startPos, currentEndPos, craftYEndPos)) {
                        //TODO: convert builder to dsl
                        val particleBuilder = ParticleBuilder(world,
                                currentEndPos,
                                timePassed,
                                duration,
                                craftYEndPos,
                                itemStack)

                        // Shot particles from the 4 corners of the hologram
                        particleBuilder.shoot(startPos)
                        particleBuilder.shoot(hologramPos + Vec3d(0.2, 0.5, 0.8))
                        particleBuilder.shoot(hologramPos + Vec3d(0.8, 0.5, 0.2))
                        particleBuilder.shoot(hologramPos + Vec3d(0.8, 0.5, 0.8))
                    }
                }
            }
        }


    }

    fun calcEndPos(ticksPassed: Duration, totalDuration: Duration, origEndPos: Vec3d, craftEndY: Double): Vec3d {
        var endY: Double
        endY = if (ticksPassed >= PHASE_2_START) {
            val phase2TicksPassed = ticksPassed - PHASE_2_START
            origEndPos.y + PHASE_2_Y_END_POS_INCREASE_PER_TICK * phase2TicksPassed.inTicks
        }


        // Calculates phase 3 start time based on how close the crafting is to ending
        else {
            origEndPos.y
        }

        val phase3StartTime = (totalDuration - ((endY - craftEndY) / SLAMDOWN_SPEED_BLOCKS_PER_TICK).roundToInt().ticks)
        if (ticksPassed >= phase3StartTime) {
            val phase3TicksPassed = ticksPassed - phase3StartTime
            endY = Math.max(craftEndY, endY - SLAMDOWN_SPEED_BLOCKS_PER_TICK * phase3TicksPassed.inTicks)
        }
        return Vec3d(origEndPos.x, endY, origEndPos.z)
    }

    fun calcSpeed(timePassed: Duration): Double {
        return SPEED_BLOCKS_PER_TICK_BASE + SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK * timePassed.inTicks
    }

    // This is used to make particles stop appearing slightly before the crafting stops, such that it looks like once ALL particles have stopped the crafting is done.
    private fun getRelativeTimePassed(timePassed: Duration, totalDuration: Duration, startPos: Vec3d, origEndPos: Vec3d, endYLoc: Double): Duration {
        val endPos = calcEndPos(timePassed, totalDuration, origEndPos, endYLoc)
        return (timePassed + (MathUtil.minimalDistanceOf(startPos, endPos) / calcSpeed(timePassed)).roundToInt().ticks)
    }


}