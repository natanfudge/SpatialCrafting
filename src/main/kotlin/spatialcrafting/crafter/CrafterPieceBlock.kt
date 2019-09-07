package spatialcrafting.crafter

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import spatialcrafting.client.particle.playRoundOfCraftParticles
import spatialcrafting.ticker.Scheduleable
import spatialcrafting.util.isServer
import spatialcrafting.util.logDebug
import spatialcrafting.util.logWarning
import spatialcrafting.util.xz


val CraftersPieces = mapOf(
        2 to CrafterPieceBlock(2),
        3 to CrafterPieceBlock(3),
        4 to CrafterPieceBlock(4),
        5 to CrafterPieceBlock(5)
)

inline fun <reified T> BlockEntity?.assertIs(pos: BlockPos? = null, world: IWorld? = null): T {
    val result = this as? T
    return (result
            ?: error("BlockEntity at location ${pos
                    ?: this?.pos} is not a ${T::class.qualifiedName} as expected. Rather, it is '${this
                    ?: "AIR"}'." + if (world != null) " This happened on the ${if (world.isClient) "CLIENT" else "SERVER"}" else ""))

}

//inline fun <reified T> World.get(pos : BlockPos) : T = world.getBlockEntity(pos).assertIs(pos)
//inline fun <reified T> World.getNullable(pos : BlockPos) : T? = world.getBlockEntity(pos) as? T


fun World.getCrafterEntity(pos: BlockPos) = world.getBlockEntity(pos).assertIs<CrafterPieceEntity>(pos)
fun World.getCrafterEntityOrNull(pos: BlockPos) = world.getBlockEntity(pos) as? CrafterPieceEntity
class CrafterPieceBlock(val size: Int) : Block(Settings.copy(
        when (size) {
            2 -> Blocks.OAK_LOG
            3 -> Blocks.STONE
            4 -> Blocks.IRON_BLOCK
            5 -> Blocks.DIAMOND_BLOCK
            else -> error("unexpected size")
        }
)), BlockEntityProvider, Scheduleable {


    companion object {
        const val FinishCraft = 1
        const val BeginCraftSoundLoop = 2
        const val EmitRoundOfCraftingParticles = 3
    }

    override fun neighborUpdate(blockState_1: BlockState?, world: World, pos: BlockPos, block_1: Block?, blockPos_2: BlockPos?, boolean_1: Boolean) {
        //TODO: think of a better solution
        // Maybe on chunk update, look at gravel

        super.neighborUpdate(blockState_1, world, pos, block_1, blockPos_2, boolean_1)
    }

    override fun createBlockEntity(var1: BlockView?) = CrafterPieceEntity()

    override fun buildTooltip(itemstack: ItemStack, blockView: BlockView?, tooltip: MutableList<Text>, tooltipContext: TooltipContext) {
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_1", size * size, size, size))
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_2"))
    }


    override fun onBlockRemoved(blockState: BlockState, world: World, pos: BlockPos, blockState_2: BlockState?, boolean_1: Boolean) {
        assert(world.isServer)
        val multiblock = world.getCrafterEntity(pos).multiblockIn
                ?: return super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
        if(multiblock.isCrafting) multiblock.stopCrafting(world)
        destroyMultiblockFromServer(world, multiblock)
        super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
    }


    override fun onScheduleEnd(world: World, pos: BlockPos, scheduleId: Int, additionalData: CompoundTag) {
        assert(world.isServer)
        logDebug { "Scheduling ended at world time ${world.time}" }
        when (scheduleId) {
            FinishCraft -> attemptToFinishCraft(world, pos)
            BeginCraftSoundLoop -> beginCraftSoundLoop(world, pos)
            EmitRoundOfCraftingParticles -> playRoundOfCraftParticles(world, pos, additionalData)
            else -> logWarning { "Nothing is expected to be scheduled with id $scheduleId" }
        }


    }


    override fun activate(blockState_1: BlockState, world: World, pos: BlockPos, clickedBy: PlayerEntity?,
                          hand: Hand, blockHitResult_1: BlockHitResult?): Boolean {

        // Prevent it being called twice
        if (hand == Hand.OFF_HAND) return false

        logDebug {
            val multiblockIn = world.getCrafterEntity(pos).multiblockIn
            "${if (world.isClient) "CLIENT" else "SERVER"}: Right clicked on crafter piece at ${pos.xz}. Formed = ${multiblockIn != null}"
        }
        val multiblockIn = world.getCrafterEntity(pos).multiblockIn ?: return false



        if (world.isClient || multiblockIn.isCrafting) return true

        val matches = multiblockIn.getMatchingRecipes(world)

        if (matches.isEmpty()) {
            clickedBy?.sendMessage(TranslatableText("message.spatialcrafting.no_match"))
            return true
        }
        else craft(matches, world, multiblockIn, pos)

        return true
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        attemptToFormMultiblock(world, blockPos, placedBy)
    }


}

