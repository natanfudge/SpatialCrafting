package spatialcrafting.util.kotlinwrappers

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World

fun World.getBlock(location: BlockPos): Block = getBlockState(location).block
val World.isServer get() = !isClient
/**
 * Replaces the block in the [pos] with the specified [block], using the default [BlockState].
 */
fun IWorld.setBlock(block: Block, pos: BlockPos) : Boolean = world.setBlockState(pos, block.defaultState)