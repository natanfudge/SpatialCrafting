package spatialcrafting.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.client.ParticleUtil.calcEndPos
import spatialcrafting.client.ParticleUtil.calcSpeed
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.util.d
import kotlin.math.roundToInt


data class ParticleBuilder(val world: World, val endPos: Vec3d, val startTimeDelay: Duration, val craftDuration: Duration,
                           val craftYEndPos: Double, val stack: ItemStack) {
    fun shoot(startPos: Vec3d) {
        shootCraftParticle(world, startPos, endPos, startTimeDelay, craftDuration, craftYEndPos, stack)
    }
}

fun BlockPos.toVec3d() = Vec3d(x.d, y.d, z.d)
operator fun Vec3d.plus(other: Vec3d) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)

//TODO: implement properly
fun CrafterMultiblock.centerOfHolograms(): Vec3d = hologramLocations[0].toVec3d()


//
////TODO document particles
//@Environment(EnvType.CLIENT)
//private fun shootCraftParticle(world: World, startPos: Vec3d, endPos: Vec3d, startTimeDelay: Int, craftDuration: Int, craftYEndPos: Double, stack: ItemStack) {
//    val texture = MinecraftClient.getInstance().itemRenderer.getHeldItemModel(stack, world, null).sprite
//    MinecraftClient.getInstance().particleManager.addParticle(ParticleCraft(world, startPos, endPos, startTimeDelay, craftDuration, craftYEndPos, texture))
//}

// This is used to make particles stop appearing slightly before the crafting stops, such that it looks like once ALL particles have stopped the crafting is done.
fun getRelativeTicksPassed(timePassed: Duration, totalDuration: Duration, startPos: Vec3d, origEndPos: Vec3d, endYLoc: Double): Duration {
    val endPos: Vec3d = calcEndPos(timePassed, totalDuration, origEndPos, endYLoc)
    return (timePassed + (MathUtil.minimalDistanceOf(startPos, endPos) / calcSpeed(timePassed)).roundToInt().ticks)
}

@Environment(EnvType.CLIENT)
fun shootCraftParticle(world: World, startPos: Vec3d, endPos: Vec3d, startTimeDelay: Duration, craftDuration: Duration,
                       currentEndY: Double, stack: ItemStack) {
    MinecraftClient.getInstance().particleManager.addParticle(
            CraftParticle(
                    world = world,
                    sourcePos = startPos,
                    endX = endPos.x,
                    origEndY = endPos.y,
                    endZ = endPos.z,
                    itemStack = stack,
                    craftDuration = craftDuration,
                    currentEndY = currentEndY,
                    startTimeDelay = startTimeDelay
            )
    )
}