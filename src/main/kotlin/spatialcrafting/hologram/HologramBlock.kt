@file:Suppress("DEPRECATION")

package spatialcrafting.hologram

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import net.minecraft.block.*
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import spatialcrafting.client.gui.RecipeCreatorGui
import spatialcrafting.client.gui.RecipeCreatorScreen
import spatialcrafting.client.keybinding.MinimizeHologramsKeyBinding
import spatialcrafting.client.keybinding.RecipeCreatorKeyBinding
import spatialcrafting.crafter.stopCrafting
import spatialcrafting.hologram.HologramBlock.IsHiddenPropertyName
import spatialcrafting.util.*


private const val Unbreakable = -1.0f
private const val Indestructible = 3600000.0f

private val HologramSettings = Builders.blockSettingsOfMaterial(
        Material.STRUCTURE_VOID,
        collidable = false,
//        materialColor = MaterialColor.WHITE,
//        blocksLight = false,
//        blocksMovement = false,wher
//        burnable = false,
        hardness = Unbreakable,
        resistance = Indestructible
//        isLiquid = false,
//        isSolid = false,
//        pistonBehavior = PistonBehavior.IGNORE,
//        replaceable = false
)


val IsHidden: BooleanProperty = BooleanProperty.of(IsHiddenPropertyName)

object HologramBlock : Block(HologramSettings), BlockEntityProvider, AttributeProvider, InventoryProvider {
    const val IsHiddenPropertyName = "is_hidden"


    override fun getInventory(blockState: BlockState?, world: IWorld, pos: BlockPos): SidedInventory {
        return HologramInventoryWrapper(world.getHologramEntity(pos).inventory, pos)
    }

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean = true

    override fun canSuffocate(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean = false

    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?) = false

    override fun allowsSpawning(state: BlockState?, view: BlockView?, pos: BlockPos?, type: EntityType<*>?) = false

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

    override fun appendProperties(stateFactory: StateManager.Builder<Block, BlockState>) {
        stateFactory.add(IsHidden)
    }

    init {
        defaultState = stateManager.defaultState.with(IsHidden, false)
    }

    private val halfBlock = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)


    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos, ePos: EntityContext): VoxelShape {
        return when {
            state.get(IsHidden) -> VoxelShapes.empty()
            MinimizeHologramsKeyBinding.isPressed -> halfBlock
            else -> super.getOutlineShape(state, view, pos, ePos)
        }
    }


    override fun getRenderType(blockState: BlockState): BlockRenderType {
        return if (blockState.get(IsHidden)) BlockRenderType.INVISIBLE else super.getRenderType(blockState)
    }


    override fun onUse(blockState: BlockState, world: World, pos: BlockPos, clickedBy: PlayerEntity?, hand: Hand?, blockHitResult_1: BlockHitResult?): ActionResult {
        if (clickedBy == null || hand == null) return ActionResult.FAIL


        val hologramEntity = world.getHologramEntity(pos)


        if (clickedBy.isHoldingItemIn(hand)) {
            if (hologramEntity.isEmpty()) {
                hologramEntity.insertItem(clickedBy.getStackInHand(hand))
                if (!clickedBy.isCreative) clickedBy.getStackInHand(hand).count--
                logDebug {
                    "Inserted item into hologram. New Content: " + hologramEntity.getItem()
                }
            }
        }

        if (world.isClient && clickedBy.isCreative && RecipeCreatorKeyBinding.isPressed) {
            getMinecraftClient().openScreen(RecipeCreatorScreen(RecipeCreatorGui()))
        }
        return ActionResult.SUCCESS
    }


    override fun onBreak(world: World, pos: BlockPos, blockState: BlockState?, player: PlayerEntity) {
        if (world.isClient) return
        val hologramEntity = world.getHologramEntity(pos)
        // This is to make it so in creative mod you won't get unnecessary items. (onBlockRemoved is called afterwards)
        val extractedItem = hologramEntity.extractItem()
        // Cancel crafting if needed
        if (!extractedItem.isEmpty) {
            val multiblock = hologramEntity.getMultiblockOrNull()
                    ?: hologramEntity.complainAboutMissingMultiblock().run { return }
            if (multiblock.isCrafting) multiblock.stopCrafting(world)

        }

    }


    override fun onBlockRemoved(stateBefore: BlockState, world: World, pos: BlockPos, stateAfter: BlockState, boolean_1: Boolean) {
        // Only happens when the entire multiblock is destroyed or in creative mode.
        if (stateBefore.block != stateAfter.block) {
            world.getHologramEntity(pos).dropInventory()
        }

    }

    override fun onBroken(world: IWorld, pos: BlockPos, blockState: BlockState) {
        // For creative mode
        world.setBlock(HologramBlock, pos)
        logDebug {
            "Left Click on hologram in position $pos. Block Entity: ${world.getBlockEntity(pos)}"
        }


        super.onBroken(world, pos, blockState)
    }


    override fun onBlockBreakStart(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity?) {
        giveItemInHologramToPlayer(player, world, pos)
    }

    private fun giveItemInHologramToPlayer(player: PlayerEntity?, world: World, pos: BlockPos) {
        if (player == null) return
        val itemInHologram = world.getHologramEntity(pos).extractItem()
        player.offerOrDrop(itemInHologram)
    }


}

fun IWorld.getHologramEntity(pos: BlockPos): HologramBlockEntity {
    return getBlockEntity(pos).assertIs(pos, this)
}