package spatialcrafting.client.particle

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.client.Speed
import spatialcrafting.client.bps
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.util.Duration
import spatialcrafting.util.distancePassedIn
import spatialcrafting.util.seconds
import spatialcrafting.util.ticks
import kotlin.math.max
import kotlin.math.roundToInt

private val TimeBetweenParticles = 0.1.seconds
private fun vec3d(x: Double, y: Double, z: Double) = Vec3d(x, y, z)
private operator fun BlockPos.plus(vec3d: Vec3d) = this.toVec3d() + vec3d

private val YEndPosIncreaseSpeed = 0.5.bps
private val SlamdownSpeed = 4.bps
private val BaseBlockFlightSpeed = 1.bps
private val BlockFlightSpeedIncreasePerSecond = 0.2.bps
private val Phase2StartTime = 5.seconds


@Environment(EnvType.CLIENT)
fun playCraftParticles(world: World, multiblock: CrafterMultiblock, duration: Duration) {
    val hologramParticleData = multiblock.getHologramEntities(world).map {
        HologramParticleData(
                // Shoots particles from the 4 corners of the hologram
                corner1Location = it.pos + vec3d(0.2, 0.5, 0.2),
                corner2Location = it.pos + vec3d(0.2, 0.5, 0.8),
                corner3Location = it.pos + vec3d(0.8, 0.5, 0.2),
                corner4Location = it.pos + vec3d(0.8, 0.5, 0.8),
                itemStack = it.getItem()
        )
    }

    CraftParticleEmitter(world = world,
            craftDuration = duration,
            //TODO this value is responsible on some way to the y value of where the particles end
            // at the end of the craft, but it doesn't seem to work properly
            craftYEndPos = multiblock.centerOfHolograms().y /*- 1.0*/,  /*+ 6.5*/
            originalEndPos = multiblock.centerOfHolograms(),
            allHologramData = hologramParticleData,
            startTime = world.time,
            multiblock = multiblock
    ).emit()
}



data class HologramParticleData(val corner1Location: Vec3d,
                                val corner2Location: Vec3d,
                                val corner3Location: Vec3d,
                                val corner4Location: Vec3d,
                                val itemStack: ItemStack)

class CraftParticleEmitter(val world: World, val craftDuration: Duration, val originalEndPos: Vec3d,
                           val craftYEndPos: Double, val allHologramData: List<HologramParticleData>, val startTime: Long,
                           private val multiblock: CrafterMultiblock) {

    private var timePassed = 0.ticks

    fun emit() {
        GlobalScope.launch {
            while (timePassed < craftDuration
                    // Make it so we can cancel the crafting
                    && multiblock.isCrafting
                    // Make sure this stops when the player exits
                    && world == MinecraftClient.getInstance().world) {
                for (hologram in allHologramData) {
                    if (hologram.itemStack.item != Items.AIR) {
                        emitHologramParticles(hologram)
                    }
                }
                delay(TimeBetweenParticles.inMilliseconds)
                timePassed = (world.time - startTime).ticks

            }

        }
    }

    // This is used to make particles stop appearing slightly before the crafting stops, such that it looks like once ALL particles have stopped the crafting is done.
    private fun getRelativeTicksPassed(hologram: HologramParticleData): Duration {
        val endPos: Vec3d = calcEndPos(timePassed, craftDuration, originalEndPos, craftYEndPos)
        return (timePassed + (MathUtil.minimalDistanceOf(hologram.corner1Location, endPos) / calcSpeed(timePassed).blocksPerTick).roundToInt().ticks)
    }


    private fun emitHologramParticles(hologramData: HologramParticleData) {
        // Make particles stop appearing slightly before the crafting stops,
        if (craftDuration > getRelativeTicksPassed(hologramData)) {

            ParticleBuilder(world,
                    originalEndPos,
                    timePassed,
                    craftDuration,
                    craftYEndPos,
                    hologramData.itemStack).run {
                shootParticleFrom(hologramData.corner1Location)
                shootParticleFrom(hologramData.corner2Location)
                shootParticleFrom(hologramData.corner3Location)
                shootParticleFrom(hologramData.corner4Location)
            }


        }
    }


}

fun calcSpeed(timePassed: Duration): Speed {
    return BaseBlockFlightSpeed + distancePassedIn { timePassed with BlockFlightSpeedIncreasePerSecond }.bps
}

fun calcEndPos(ticksPassed: Duration, totalDuration: Duration, centerOfHolograms: Vec3d, craftEndY: Double): Vec3d {
    var endY: Double
    endY = if (ticksPassed >= Phase2StartTime) {
        val phase2TimePassed = ticksPassed - Phase2StartTime
        centerOfHolograms.y + distancePassedIn { phase2TimePassed with YEndPosIncreaseSpeed }
    }


    // Calculates phase 3 start time based on how close the crafting is to ending
    else {
        centerOfHolograms.y
    }

    val phase3StartTime = (totalDuration - ((endY - craftEndY) / SlamdownSpeed.blocksPerTick).roundToInt().ticks)
    if (ticksPassed >= phase3StartTime) {
        val phase3TicksPassed = ticksPassed - phase3StartTime
        endY = max(craftEndY, endY - distancePassedIn { phase3TicksPassed with SlamdownSpeed })
    }
    return Vec3d(centerOfHolograms.x, endY, centerOfHolograms.z)
}

