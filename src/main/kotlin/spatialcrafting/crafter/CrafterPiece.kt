package spatialcrafting.crafter

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.client.*
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.sendPacket
import spatialcrafting.util.*
import spatialcrafting.util.kotlinwrappers.dropItemStack
import spatialcrafting.util.kotlinwrappers.getBlock
import spatialcrafting.util.kotlinwrappers.isServer
import spatialcrafting.util.kotlinwrappers.setBlock
import java.util.*


val craftersPieces = listOf(
        CrafterPiece(2),
        CrafterPiece(3),
        CrafterPiece(4),
        CrafterPiece(5)
)

fun <T> BlockEntity?.assertIs(pos: BlockPos): T {
    return (this as? T)
            ?: error("BlockEntity at location $pos is not a Crafter Piece Entity as expected.\nRather, it is '${this
                    ?: "air"}'.")
}


fun World.getCrafterEntity(pos: BlockPos) = world.getBlockEntity(pos).assertIs<CrafterPieceEntity>(pos)
class CrafterPiece(val size: Int) : Block(Settings.copy(
        when (size) {
            2 -> Blocks.OAK_LOG
            3 -> Blocks.STONE
            4 -> Blocks.IRON_BLOCK
            5 -> Blocks.DIAMOND_BLOCK
            else -> error("unexpected size")
        }
)), BlockEntityProvider {


    companion object {
        private fun thereIsSpaceForHolograms(world: World, multiblock: CrafterMultiblock): Boolean =
                multiblock.hologramLocations.all {
                    val isAir = world.getBlock(it) is AirBlock
                    if (isAir) true
                    else {
                        if (world.isServer) logDebug { "Refusing to create multiblock due to ${world.getBlock(it)} existing in a required hologram space $it." }
                        false
                    }
                }


        private fun CrafterMultiblock.logString() = "Size = $multiblockSize, Locations = \n" + crafterLocations.groupBy { it.x }
                .entries
                .joinToString("\n") { column -> column.value.joinToString(", ") { it.xz } }

        fun createMultiblock(world: World, masterPos: BlockPos, multiblock: CrafterMultiblock) {
            if (thereIsSpaceForHolograms(world, multiblock)) {
                logDebug { "Building multiblock. [${multiblock.logString()}]" }
                CrafterPieceEntity.assignMultiblockState(world, masterPos, multiblock)

                for (hologramPos in multiblock.hologramLocations) {
                    world.setBlock(HologramBlock, pos = hologramPos)
                }


            }
            else {
                //TODO: show an indicator that there is no space
            }

        }

        fun destroyMultiblock(world: World, multiblock: CrafterMultiblock) {
            if (world.isServer) logDebug { "Destroying multiblock. [${multiblock.logString()}]" }
            CrafterPieceEntity.unassignMultiblockState(world, multiblock)

            for (hologramPos in multiblock.hologramLocations) {
                world.setBlock(Blocks.AIR, pos = hologramPos)
            }

        }
    }

    override fun createBlockEntity(var1: BlockView?) = CrafterPieceEntity()

    override fun buildTooltip(itemstack: ItemStack, blockView: BlockView?, tooltip: MutableList<Text>, tooltipContext: TooltipContext) {
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_1", size * size, size, size))
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_2"))
    }


    private fun World.holdsCompatibleCrafterPiece(blockPos: BlockPos) = getBlock(blockPos).let {
        it is CrafterPiece && it.size == this@CrafterPiece.size
    }


    override fun onBlockRemoved(blockState: BlockState, world: World, pos: BlockPos, blockState_2: BlockState?, boolean_1: Boolean) {
        assert(world.isServer)
        val multiblock = world.getCrafterEntity(pos).multiblockIn ?: return
        destroyMultiblockFromServer(world, multiblock)
        super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
    }

    private fun destroyMultiblockFromServer(world: World, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        destroyMultiblock(world, multiblock)
//        PlayerStream.watching(world,multiblock.locations[0]).sendPacket(Packets.DestroyMultiblock, Packets.DestroyMultiblock(
//                multiblock
//        ))
    }

    /**
     * Called when crafting is done
     */
    override fun onScheduledTick(blockState: BlockState?, world: World, pos: BlockPos, random: Random?) {
        logDebug { "Scheduling ended at world time ${world.time}" }
        val multiblock = world.getCrafterEntity(pos).multiblockIn ?: return
        // In case it was canceled
        val craftEndTime = multiblock.craftEndTime ?: return

        // For some reason this gets called when the world loads for no reasons os we need to do this
        if (craftEndTime > world.durationTime) {
            logDebug { "Scheduling popped too early at ${world.durationTime} when it was supposed to pop at $craftEndTime. Rescheduling." }
            world.blockTickScheduler.schedule(pos, this, craftEndTime - world.durationTime)
            return
        }

        val craftedRecipeOptional = world.recipeManager.getFirstMatch(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(multiblock.getInventory(world)), world)
        val craftedRecipe = craftedRecipeOptional
                // Can sometimes be null when the playing is loading
                .orElse(null) ?: return

        world.play(Sounds.CraftEnd, at = pos, ofCategory = SoundCategory.BLOCKS)

        multiblock.setNotCrafting(world)

        world.dropItemStack(craftedRecipe.output, multiblock.centerOfHolograms().toBlockPos())

        for (hologram in multiblock.getHologramEntities(world)) {
            hologram.extractItem()
        }

    }
    //TODO: document sounds

    override fun activate(blockState_1: BlockState, world: World, pos: BlockPos, placedBy: PlayerEntity?, hand: Hand, blockHitResult_1: BlockHitResult?): Boolean {


        if (hand == Hand.OFF_HAND) return false

        if (world.isClient) return false
        // Prevent it being called twice


        val multiblockIn = world.getCrafterEntity(pos).multiblockIn ?: return false
        if (multiblockIn.isCrafting) return false


        val matches = world.recipeManager.getAllMatches(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(multiblockIn.getInventory(world)), world)

        //TODO: provide feedback that no recipe matched
        if (matches.isEmpty()) return false
        if (matches.size > 1) {
            println("[Spatial Crafting] WARNING: THERE IS MORE THAN ONE RECIPE THAT MATCHES THE SAME INPUT!" +
                    "ONLY THE FIRST RECIPE WILL BE USED! The recipes are: \n$matches")
        }

        val craftDuration = 15.seconds
        val endTime = world.durationTime + craftDuration

        multiblockIn.setIsCrafting(world, craftEndTime = endTime)
        world.play(Sounds.CraftStart, at = pos, ofCategory = SoundCategory.BLOCKS)

        GlobalScope.launch {
            while (multiblockIn.isCrafting) {
                // We do the min here so in case the time remaining is not exactly the duration time it still stops when the crafting ends.
                delay(min(multiblockIn.craftEndTime!! - world.durationTime, Sounds.CraftLoopDuration))
                world.play(Sounds.CraftLoop, at = pos, ofCategory = SoundCategory.BLOCKS)
//                world.sound
            }
        }

        PlayerStream.watching(world.getBlockEntity(pos)).sendPacket(
                Packets.StartCraftingParticles(multiblockIn, craftDuration)
        )

        // Schedule the crafting to end
        world.blockTickScheduler.schedule(pos, this, craftDuration)

        logDebug {
            "Scheduling craft at ${world.time} scheduled to end at $endTime"
        }


        return false
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        val northernEasternCrafter = getNorthernEasternCrafter(world, blockPos)
        val multiblock = attemptToFormMultiblock(world, northernEasternCrafter) ?: return

        createMultiblockFromServer(world, northernEasternCrafter, multiblock)
    }

    private fun createMultiblockFromServer(world: World, northernEasternCrafter: BlockPos, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        createMultiblock(
                world = world,
                masterPos = northernEasternCrafter,
                multiblock = multiblock
        )
//        PlayerStream.watching(world,multiblock.locations[0]).sendPacket(Packets.CreateMultiblock, Packets.CreateMultiblock(
//                multiblock = multiblock,
//                masterEntityLocation = northernEasternCrafter
//        ))
    }


    private fun attemptToFormMultiblock(world: World, northernEasternCrafterPos: BlockPos): CrafterMultiblock? {
        val blocks = mutableListOf<BlockPos>()
        for ((westDistance, southDistance) in (0 to 0) until (size to size)) {
            val location = northernEasternCrafterPos.west(westDistance).south(southDistance)
            if (!world.holdsCompatibleCrafterPiece(location)) {
                logDebug { "Refusing to build multiblock due to ${world.getBlock(location)} existing in required position ${location.xz}" }
                return null // All nearby blocks must be crafter pieces
            }
            blocks.add(location)

        }

        return CrafterMultiblock(blocks, size, craftEndTime = null)
    }


    private fun getNorthernEasternCrafter(world: World, blockPos: BlockPos): BlockPos {
        var currentBlock = blockPos
        // Only go as far as size - 1 blocks away
        repeat(size - 1) {
            with(world) {
                val northernBlock = currentBlock.north()
                val easternBlock = currentBlock.east()
                val northernEasternBlock = currentBlock.north().east()

                when {
                    holdsCompatibleCrafterPiece(northernEasternBlock) -> currentBlock = northernEasternBlock
                    holdsCompatibleCrafterPiece(northernBlock) -> currentBlock = northernBlock
                    holdsCompatibleCrafterPiece(easternBlock) -> currentBlock = easternBlock
                }

            }
        }
        return currentBlock
    }


}

