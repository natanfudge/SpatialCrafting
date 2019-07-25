package spatialcrafting

import net.minecraft.block.*
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateFactory
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import spatialcrafting.util.Builders
import spatialcrafting.util.setBlock

private const val Unbreakable = -1.0f
private const val Indestructible = 3600000.0f

private val HologramSettings = Builders.blockSettings(
        collidable = false,
        materialColor = MaterialColor.AIR,
        blocksLight = false,
        blocksMovement = false,
        burnable = false,
        hardness = Unbreakable,
        resistance = Indestructible,
        isLiquid = false,
        isSolid = false,
        pistonBehavior = PistonBehavior.IGNORE,
        replaceable = false
)
val HologramIndestructible = BooleanProperty.of("destructible")

object HologramBlock : Block(HologramSettings), BlockEntityProvider {
    // This must be set to false to make be able to remove an hologram


    init {
        defaultState = stateFactory.defaultState.with(HologramIndestructible, true)
    }

    override fun createBlockEntity(var1: BlockView?) = HologramBlockEntity()

    override fun appendProperties(stateFactory: StateFactory.Builder<Block, BlockState>) {
        stateFactory.add(HologramIndestructible)
    }



    //TODO: add blockState and document in wiki


    override fun getRenderLayer(): BlockRenderLayer {
        return BlockRenderLayer.TRANSLUCENT
    }


    override fun getOutlineShape(blockState_1: BlockState?, blockView_1: BlockView?, blockPos_1: BlockPos?, entityContext_1: EntityContext?): VoxelShape {
        return super.getOutlineShape(blockState_1, blockView_1, blockPos_1, entityContext_1)
        //TODO change based on state
//        return VoxelShapes.cuboid(0.0,0.0,0.0,0.0,0.0,0.0)
    }


    override fun getRenderType(blockState_1: BlockState?): BlockRenderType {
        return super.getRenderType(blockState_1)
        //TODO: change based on state
//        return BlockRenderType.INVISIBLE
    }

    override fun activate(blockState_1: BlockState?, world_1: World?, blockPos_1: BlockPos?, playerEntity_1: PlayerEntity?, hand_1: Hand?, blockHitResult_1: BlockHitResult?): Boolean {
        //TODO: insert item into hologram
        return super.activate(blockState_1, world_1, blockPos_1, playerEntity_1, hand_1, blockHitResult_1)
    }

    override fun onBlockRemoved(stateBefore: BlockState, world: World, pos: BlockPos, stateAfter: BlockState, boolean_1: Boolean) {
        ensureBlockIsNotManuallyDestructible(stateAfter, stateBefore, world, pos)
    }

    /**
     * This is a workaround for the block not being destructible in creative
     */
    private fun ensureBlockIsNotManuallyDestructible(stateAfter: BlockState, stateBefore: BlockState, world: World, pos: BlockPos) {
        if (
                stateAfter.block !is HologramBlock // No need to put a new hologram block if afterwards there will be a hologram
                && !(stateAfter.isAir && !stateBefore.get(HologramIndestructible))) // This is the situation in which we explicitly remove the hologram when the multiblock is destroyed. In that case we shouldn't put a new hologram.
        {
            world.setBlock(HologramBlock, pos)
            world.getHologramEntity(pos).dropInventory()
        }
    }

    fun World.getHologramEntity(pos :BlockPos) = getBlockEntity(pos).assertIs<HologramBlockEntity>()

    override fun onBlockBreakStart(blockState: BlockState, world: World, pos: BlockPos, playerEntity_1: PlayerEntity?) {
        world.getHologramEntity(pos).dropInventory()
        val x = 2
    }


}