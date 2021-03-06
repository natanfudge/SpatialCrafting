@file:Suppress("DEPRECATION")

package spatialcrafting.crafter

import fabricktx.api.*
import net.minecraft.block.*
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import scheduler.Scheduleable
import spatialcrafting.client.particle.playRoundOfCraftParticles
import spatialcrafting.logDebug
import spatialcrafting.logWarning




//fun craf

private fun settings(size: Int) = AbstractBlock.Settings.of(when (size) {
    2 -> Material.WOOD
    3 -> Material.STONE
    4 -> Material.ANVIL
    5 -> Material.METAL
    else -> error("unexpected size")
})


fun World.getCrafterEntity(pos: BlockPos) = world.getBlockEntity(pos).assertIs<CrafterPieceEntity>(pos, this)
fun World.getCrafterEntityOrNull(pos: BlockPos) = world.getBlockEntity(pos) as? CrafterPieceEntity
class CrafterPieceBlock(val size: Int) : StateBlock<CrafterPieceEntity>(settings(size), ::CrafterPieceEntity),
         Scheduleable, InventoryProvider {

    override fun getInventory(blockState: BlockState, world: IWorld, pos: BlockPos): SidedInventory {
        if (world is World && world.isServer && Thread.currentThread() == world.server!!.thread) {
            return CrafterPieceInventoryDelegator(pos, world, this)
        } else return EmptyInventory
    }


    companion object {
        private val craftersPieces = mapOf(
                2 to CrafterPieceBlock(2),
                3 to CrafterPieceBlock(3),
                4 to CrafterPieceBlock(4),
                5 to CrafterPieceBlock(5)
        )

        val All = BlockList(craftersPieces.values.toList())
        fun ofSize(size: Int) = craftersPieces.getValue(size)

        const val FinishCraft = 1
        const val BeginCraftSoundLoop = 2
        const val EmitRoundOfCraftingParticles = 3

        const val CraftIsAutomatedKey = "automated"
    }

    override fun neighborUpdate(blockState_1: BlockState?, world: World, pos: BlockPos, block_1: Block?, blockPos_2: BlockPos?, boolean_1: Boolean) {
        //TODO: think of a better solution
        // Maybe on chunk update, look at gravel

        super.neighborUpdate(blockState_1, world, pos, block_1, blockPos_2, boolean_1)
    }

    override fun buildTooltip(itemstack: ItemStack, blockView: BlockView?, tooltip: MutableList<Text>, tooltipContext: TooltipContext) {
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_1", size * size, size, size))
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_2"))
        if (size >= 3) {
            tooltip.add(LiteralText(""))
            tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_big_1"))
            tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_big_2"))
        }
    }


    override fun onBlockRemoved(blockState: BlockState, world: World, pos: BlockPos, blockState_2: BlockState?, boolean_1: Boolean) {
        assert(world.isServer)
        val multiblock = world.getCrafterEntity(pos).multiblockIn
                ?: return super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
        if (multiblock.isCrafting) multiblock.stopCrafting(world)
        destroyMultiblockFromServer(world, multiblock)
        super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
    }


    override fun onScheduleEnd(world: World, pos: BlockPos, scheduleId: Int, additionalData: CompoundTag) {
        assert(world.isServer)
        logDebug { "Scheduling ended at world time ${world.time}" }
        when (scheduleId) {
            FinishCraft -> attemptToFinishCraft(world, pos, stopRecipeHelp = !additionalData.getBoolean(CraftIsAutomatedKey))
            BeginCraftSoundLoop -> beginCraftSoundLoop(world, pos)
            //TODO: very possible this won't ever work
            EmitRoundOfCraftingParticles -> playRoundOfCraftParticles(world, pos, additionalData)
            else -> logWarning { "Nothing is expected to be scheduled with id $scheduleId" }
        }


    }


    override fun onUse(blockState_1: BlockState, world: World, pos: BlockPos, clickedBy: PlayerEntity?,
                       hand: Hand, blockHitResult_1: BlockHitResult?): ActionResult {
        // 5 71 1-51

        // Prevent it being called twice
        if (hand == Hand.OFF_HAND) return ActionResult.FAIL

        logDebug {
            val multiblockIn = world.getCrafterEntity(pos).multiblockIn
            "${if (world.isClient) "CLIENT" else "SERVER"}: Right clicked on crafter piece at ${pos.xz}. Formed = ${multiblockIn != null}"
        }
        val multiblockIn = world.getCrafterEntity(pos).multiblockIn ?: return ActionResult.FAIL



        if (world.isClient || multiblockIn.isCrafting) return ActionResult.SUCCESS

        val matches = multiblockIn.getMatchingRecipes(world)

        if (matches.isEmpty()) {
            clickedBy?.sendMessage(TranslatableText("message.spatialcrafting.no_match"),true)
            return ActionResult.SUCCESS
        } else craft(matches, world, multiblockIn, pos, automated = false)

        return ActionResult.SUCCESS
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        attemptToFormMultiblock(world, blockPos, placedBy)
    }


}

