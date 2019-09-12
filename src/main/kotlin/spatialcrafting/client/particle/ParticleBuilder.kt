package spatialcrafting.client.particle

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.util.Duration


data class ParticleBuilder(val world: World, val originalEndPos: Vec3d, val startTimeDelay: Duration, val craftDuration: Duration,
                           val craftYEndPos: Double, val stack: ItemStack) {
    fun shootParticleFrom(startPos: Vec3d) {
        shootCraftParticle(world, startPos, originalEndPos, startTimeDelay, craftDuration, craftYEndPos, stack)
    }
}



fun CrafterMultiblock.centerOfHolograms(): Vec3d {
    val locations = hologramLocations
    // Need to add (1,1,1) because for example if you have a 3x3x3 multiblock, and the bottom left is (1,1,1),
    // the top right is (4,4,4), not (3,3,3).
    val topLeft = locations.maxBy { it.x + it.y + it.z }!!.add(1, 1, 1)
    val bottomRight = locations.minBy { it.x + it.y + it.z }!!
    return Vec3d((topLeft.x + bottomRight.x) / 2.0, (topLeft.y + bottomRight.y) / 2.0, (topLeft.z + bottomRight.z) / 2.0)
}




@Environment(EnvType.CLIENT)
fun shootCraftParticle(world: World, startPos: Vec3d, originalEndPos: Vec3d, startTimeDelay: Duration, craftDuration: Duration,
                       currentEndY: Double, stack: ItemStack) {
    MinecraftClient.getInstance().particleManager.addParticle(
            CraftParticle(
                    world = world,
                    sourcePos = startPos,
                    endX = originalEndPos.x,
                    origEndY = originalEndPos.y,
                    endZ = originalEndPos.z,
                    itemStack = stack,
                    craftDuration = craftDuration,
                    currentEndY = currentEndY,
                    startTimeDelay = startTimeDelay
            )
    )
}