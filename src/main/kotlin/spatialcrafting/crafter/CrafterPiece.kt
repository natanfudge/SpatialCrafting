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
import net.minecraft.world.IWorld
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


val CraftersPieces = mapOf(
        2 to CrafterPiece(2),
        3 to CrafterPiece(3),
        4 to CrafterPiece(4),
        5 to CrafterPiece(5)
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
                        if (world.isServer) logDebug { "Refusing to create multiblock due to a '${world.getBlock(it)}' blocking the space for a hologram in $it." }
                        false
                    }
                }


        private fun CrafterMultiblock.logString() = "Size = $multiblockSize, Locations = \n" + crafterLocations.groupBy { it.x }
                .entries
                .joinToString("\n") { column -> column.value.joinToString(", ") { it.xz } }

        fun createMultiblock(world: World, masterPos: BlockPos, multiblock: CrafterMultiblock) {
            assert(world.isServer)

            logDebug { "Building multiblock. [${multiblock.logString()}]" }
            CrafterPieceEntity.assignMultiblockState(world, masterPos, multiblock)

            for (hologramPos in multiblock.hologramLocations) {
                world.setBlock(HologramBlock, pos = hologramPos)
            }


        }

        fun destroyMultiblock(world: World, multiblock: CrafterMultiblock) {
            assert(world.isServer)
            logDebug { "Destroying multiblock.\n [${multiblock.logString()}]" }
            CrafterPieceEntity.unassignMultiblockState(world, multiblock)

            for (hologramPos in multiblock.hologramLocations) {
                world.setBlock(Blocks.AIR, pos = hologramPos)
            }

        }
    }

    override fun neighborUpdate(blockState_1: BlockState?, world: World, pos: BlockPos, block_1: Block?, blockPos_2: BlockPos?, boolean_1: Boolean) {
//        if (world.isClient) return
//        val multiblock = world.getCrafterEntity(pos).multiblockIn
//        if (multiblock == null) {
//            attemptToFormMultiblock(world, pos)
//        }

        //TODO: think of a better solution

        super.neighborUpdate(blockState_1, world, pos, block_1, blockPos_2, boolean_1)
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
        val multiblock = world.getCrafterEntity(pos).multiblockIn
                ?: return super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
        destroyMultiblockFromServer(world, multiblock)
        super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
    }

    private fun destroyMultiblockFromServer(world: World, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        destroyMultiblock(world, multiblock)
        PlayerStream.watching(world, multiblock.crafterLocations[0])
                .sendPacket(Packets.UnassignMultiblockState(multiblock))
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
                CrafterMultiblockInventoryWrapper(multiblock.getInventory(world), crafterSize = size), world)
        val craftedRecipe = craftedRecipeOptional
                // Can sometimes be null when the playing is loading
                .orElse(null) ?: return

        finishCraft(world, pos, multiblock, craftedRecipe)

    }

    private fun finishCraft(world: World, pos: BlockPos, multiblock: CrafterMultiblock, craftedRecipe: SpatialRecipe) {
        world.play(Sounds.CraftEnd, at = pos, ofCategory = SoundCategory.BLOCKS)

        multiblock.setNotCrafting(world)

        world.dropItemStack(craftedRecipe.outputStack, multiblock.centerOfHolograms().toBlockPos())

        for (hologram in multiblock.getHologramEntities(world)) {
            hologram.extractItem()
        }


        multiblock.stopRecipeHelp(world)
    }


    override fun activate(blockState_1: BlockState, world: World, pos: BlockPos, placedBy: PlayerEntity?,
                          hand: Hand, blockHitResult_1: BlockHitResult?): Boolean {

        // Prevent it being called twice
        if (hand == Hand.OFF_HAND) return false

         logDebug {
             val multiblockIn = world.getCrafterEntity(pos).multiblockIn
            "${if (world.isClient) "CLIENT" else "SERVER"}: Right clicked on crafter piece at ${pos.xz}. Formed = ${multiblockIn != null}"
        }
        val multiblockIn = world.getCrafterEntity(pos).multiblockIn ?: return false

        if (world.isClient) return true
        if (multiblockIn.isCrafting) return true


        val matches = world.recipeManager.getAllMatches(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(multiblockIn.getInventory(world), crafterSize = size), world)

        //TODO: provide feedback that no recipe matched
        if (matches.isEmpty()) return true
        else craft(matches, world, multiblockIn, pos)

        return true
    }

    private fun craft(matches: MutableList<SpatialRecipe>, world: World, multiblockIn: CrafterMultiblock, pos: BlockPos) {
        assert(world.isServer)
        if (matches.size > 1) {
            println("[Spatial Crafting] WARNING: THERE IS MORE THAN ONE RECIPE THAT MATCHES THE SAME INPUT!" +
                    "ONLY THE FIRST RECIPE WILL BE USED! The recipes are: \n$matches")
        }

        val craftDuration = matches[0].craftTime
        val endTime = world.durationTime + craftDuration

        multiblockIn.setIsCrafting(world, craftEndTime = endTime)
        playCraftingSounds(world, pos, multiblockIn)

        //TODO: show only holograms with item when starting crafting, then return to the original state when canceled / finished.
        PlayerStream.watching(world.getBlockEntity(pos)).sendPacket(
                Packets.StartCraftingParticles(multiblockIn, craftDuration)
        )

        multiblockIn.showHologramsWithItemOnly(world)

        // Schedule the crafting to end
        world.blockTickScheduler.schedule(pos, this, craftDuration)

        logDebug {
            "Scheduling craft at ${world.time} scheduled to end at $endTime"
        }
    }


    private fun playCraftingSounds(world: World, pos: BlockPos, multiblockIn: CrafterMultiblock) {
        world.play(Sounds.CraftStart, at = pos, ofCategory = SoundCategory.BLOCKS)

        GlobalScope.launch {
            while (multiblockIn.isCrafting) {
                delay(Sounds.CraftLoopDuration)
                world.play(Sounds.CraftLoop, at = pos, ofCategory = SoundCategory.BLOCKS)
            }
        }
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        println("OnPlaced")
        attemptToFormMultiblock(world, blockPos)
    }

    private fun attemptToFormMultiblock(world: World, blockPos: BlockPos) {
        assert(world.isServer)
        val northernEasternCrafter = getNorthernEasternCrafter(world, blockPos)
        val multiblock = findPossibleMultiblock(world, northernEasternCrafter) ?: return

        if (thereIsSpaceForHolograms(world, multiblock)) {
            createMultiblockFromServer(world, northernEasternCrafter, multiblock)
        }
        else {
            //TODO: show an indicator that there is no space
        }


    }

    private fun createMultiblockFromServer(world: World, northernEasternCrafter: BlockPos, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        createMultiblock(
                world = world,
                masterPos = northernEasternCrafter,
                multiblock = multiblock
        )
        PlayerStream.watching(world, multiblock.crafterLocations[0]).sendPacket(Packets.AssignMultiblockState(
                multiblock = multiblock,
                masterEntityPos = northernEasternCrafter
        ))
//        PlayerStream.watching(world, multiblock.crafterLocations[0]).sendOldPacket(Packets.AssignMultiblockState(
//                multiblock = multiblock,
//                masterEntityPos = northernEasternCrafter
//        ))
    }


    private fun findPossibleMultiblock(world: World, northernEasternCrafterPos: BlockPos): CrafterMultiblock? {
        val blocks = mutableListOf<BlockPos>()
        for ((westDistance, southDistance) in (0 to 0) until (size to size)) {
            val location = northernEasternCrafterPos.west(westDistance).south(southDistance)
            if (!world.holdsCompatibleCrafterPiece(location)) {
                logDebug { "Refusing to build multiblock due to a '${world.getBlock(location)}' existing in a position that needs to hold a crafter piece: ${location.xz}" }
                return null // All nearby blocks must be crafter pieces
            }
            blocks.add(location)

        }

        return CrafterMultiblock(blocks, size)
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

