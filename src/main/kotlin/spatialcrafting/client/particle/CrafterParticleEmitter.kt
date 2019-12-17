package spatialcrafting.client.particle

import fabricktx.api.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import scheduler.BlockScheduler
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceBlock
import spatialcrafting.crafter.getCrafterEntityOrNull
import spatialcrafting.logInfo
import spatialcrafting.logWarning
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.seconds

private val TimeBetweenParticles = 0.1.seconds


private val YEndPosIncreaseSpeed = 0.5.bps
private val SlamdownSpeed = 4.bps
private val BaseBlockFlightSpeed = 1.bps
private val BlockFlightSpeedIncreasePerSecond = 0.2.bps
private val Phase2StartTime = 5.seconds

private const val DurationDataKey = "particle_craft_duration"
private const val StartTimeKey = "particle_craft_start_time"

fun playAllCraftParticles(world: World, multiblock: CrafterMultiblock, duration: Duration) {
    val block = CrafterPieceBlock.All.first()
    val durationData = CompoundTag().apply {
        putDuration(DurationDataKey, duration)
        putLong(StartTimeKey, world.time)
    }
    multiblock.cancellationTokens.craftingParticles = BlockScheduler.repeatFor(duration.inTicks.toInt(),
            tickInterval = TimeBetweenParticles.inTicks.toInt(),
            block = block,
            scheduleId = CrafterPieceBlock.EmitRoundOfCraftingParticles,
            blockPos = multiblock.arbitraryCrafterPos,
            world = world,
            additionalData = durationData
    )
    // Execute the first one immediately
    block.onScheduleEnd(world, multiblock.arbitraryCrafterPos, scheduleId = CrafterPieceBlock.EmitRoundOfCraftingParticles,
            additionalData = durationData)
}

@Environment(EnvType.CLIENT)
fun playRoundOfCraftParticles(world: World, anyCrafterPos: BlockPos, particleData: CompoundTag) {
    val multiblock = world.getCrafterEntityOrNull(anyCrafterPos)?.multiblockIn ?: run {
        logInfo { "Can't find multiblock to emit craft particles with at pos $anyCrafterPos" }
        return
    }

    playRoundOfCraftParticles(world, multiblock, particleData)
}


@Environment(EnvType.CLIENT)
private fun playRoundOfCraftParticles(world: World, multiblock: CrafterMultiblock, particleData: CompoundTag) {

    val craftDuration = particleData.getDuration(DurationDataKey)
    if (craftDuration == 0.ticks) logWarning { "Craft initiated with 0 duration." }
    val craftStartTime = particleData.getLong(StartTimeKey)
    if (craftStartTime == 0L) logWarning { "Craft start time is set to 0." }


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
            //TODO this value is responsible on some way to the y value of where the particles end
            // at the end of the craft, but it doesn't seem to work properly
            craftYEndPos = multiblock.centerOfHolograms().y /*- 1.0*/,  /*+ 6.5*/
            originalEndPos = multiblock.centerOfHolograms(),
            allHologramData = hologramParticleData,
            craftDuration = craftDuration,
            timePassed = (world.time - craftStartTime).ticks

    ).emit()
}

//TODO: the objects here seem overcomplicated
data class HologramParticleData(val corner1Location: Vec3d,
                                val corner2Location: Vec3d,
                                val corner3Location: Vec3d,
                                val corner4Location: Vec3d,
                                val itemStack: ItemStack)

class CraftParticleEmitter(val world: World, val craftDuration: Duration, val originalEndPos: Vec3d,
                           val craftYEndPos: Double, val allHologramData: List<HologramParticleData>, val timePassed: Duration) {

    fun emit() {
        for (hologram in allHologramData) {
            if (hologram.itemStack.item != Items.AIR) {
                emitHologramParticles(hologram)
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

