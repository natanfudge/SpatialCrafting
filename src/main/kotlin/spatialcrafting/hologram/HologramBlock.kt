package spatialcrafting.hologram

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import net.minecraft.block.*
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.block.BlockState
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.world.IWorld
import spatialcrafting.assertIs
import spatialcrafting.util.*
import spatialcrafting.util.kotlinwrappers.Builders
import spatialcrafting.util.kotlinwrappers.isHoldingItemIn
import spatialcrafting.util.kotlinwrappers.setBlock


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

object HologramBlock : Block(HologramSettings), BlockEntityProvider, AttributeProvider {
    override fun addAllAttributes(
            world: World,
            pos: BlockPos,
            state: BlockState,
            to: AttributeList<*>
    ) {
        world.getBlockEntity(pos).let {
            if (it is HologramBlockEntity) {
                it.registerInventory(to)
            }
        }

    }
    // This must be set to false to make be able to remove an hologram


    override fun createBlockEntity(var1: BlockView?) = HologramBlockEntity()


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

    override fun activate(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity?, hand: Hand?, blockHitResult_1: BlockHitResult?): Boolean {
        if (player == null || hand == null) return false

        val hologramEntity = world.getHologramEntity(pos)

        if (player.isHoldingItemIn(hand)) {
            if (hologramEntity.isEmpty()) {
                hologramEntity.insertItem(player.getStackInHand(hand))
                if (!player.isCreative) player.getStackInHand(hand).count--
                logDebug {
                    "Inserted item into hologram. New Content: " + hologramEntity.getItem()
                }
            }
        }

        if (!hologramEntity.isEmpty()) {
            if (world.isClient) player.sendMessage("Item in hologram: ${hologramEntity.getItem()}")
        }

        return true
    }

    override fun onBroken(world: IWorld, pos: BlockPos, blockState: BlockState) {
        world.setBlock(HologramBlock, pos)
    }

    override fun onBreak(world: World, pos: BlockPos, blockState: BlockState?, player: PlayerEntity?) {
        // This is to make it so in creative mod you won't get unnecessary items
        world.getHologramEntity(pos).extractItem()
    }

    override fun onBlockRemoved(stateBefore: BlockState, world: World, pos: BlockPos, stateAfter: BlockState, boolean_1: Boolean) {
        world.getHologramEntity(pos).dropInventory()
        super.onBlockRemoved(stateBefore, world, pos, stateAfter, boolean_1)
    }


    private fun World.getHologramEntity(pos: BlockPos) = getBlockEntity(pos).assertIs<HologramBlockEntity>()

    override fun onBlockBreakStart(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity?) {
        giveItemInHologramToPlayer(player, world, pos)
    }

    private fun giveItemInHologramToPlayer(player: PlayerEntity?, world: World, pos: BlockPos) {
        if (player == null) return
        val itemInHologram = world.getHologramEntity(pos).extractItem()
        player.giveItemStack(itemInHologram)
    }


}