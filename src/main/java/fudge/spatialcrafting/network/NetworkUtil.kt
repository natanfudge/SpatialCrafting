package fudge.spatialcrafting.network

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.NetworkRegistry


object NetworkUtil {

    private const val RANGE_DEFAULT = 64.0


    @JvmOverloads
    fun createTargetPoint(world: World, pos: BlockPos, range: Double = RANGE_DEFAULT): NetworkRegistry.TargetPoint {
        return NetworkRegistry.TargetPoint(world.provider.dimension, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), range)
    }

}
