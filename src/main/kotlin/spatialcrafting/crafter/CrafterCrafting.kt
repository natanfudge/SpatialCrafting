package spatialcrafting.crafter

import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import scheduler.BlockScheduler
import spatialcrafting.Packets
import spatialcrafting.client.Sounds
import spatialcrafting.client.particle.centerOfHolograms
import spatialcrafting.crafter.CrafterPieceBlock.Companion.CraftIsAutomatedKey
import spatialcrafting.recipe.CraftingEffect
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.*

fun CrafterPieceBlock.craft(matches: List<SpatialRecipe>, world: World, multiblockIn: CrafterMultiblock, pos: BlockPos, automated: Boolean) {
    assert(world.isServer)
    if (matches.size > 1) {
        logWarning {
            "THERE IS MORE THAN ONE RECIPE THAT MATCHES THE SAME INPUT!\n" +
                    "ONLY THE FIRST RECIPE WILL BE USED! The recipes are: \n$matches"
        }
    }

    val recipe = matches[0]

    val craftDuration = recipe.craftTime
    val endTime = world.durationTime + craftDuration

    playCraftingSounds(world, multiblockIn.arbitraryCrafterPos)

    PlayerStream.watching(world.getBlockEntity(pos)).sendPacket(
            Packets.StartCraftingParticles(multiblockIn.arbitraryCrafterPos, craftDuration.inTicks, recipe.craftingEffect)
    )

    multiblockIn.showHologramsWithItemOnly(world)

    multiblockIn.cancellationTokens.craftingFinish = BlockScheduler.schedule(block = this,
            world = world,
            ticksUntilEnd = craftDuration.inTicks.toInt(),
            blockPos = pos,
            scheduleId = CrafterPieceBlock.FinishCraft,
            additionalData = CompoundTag().apply { putBoolean(CraftIsAutomatedKey, automated) }
    )


    logDebug {
        "Scheduling craft at ${world.time} scheduled to end at $endTime"
    }
}

private const val CraftStartDuration = (1.936281f * TicksPerSecond).toInt()

private fun CrafterPieceBlock.playCraftingSounds(world: World, pos: BlockPos) {
    assert(world.isServer)
    world.play(Sounds.CraftStart, at = pos, ofCategory = SoundCategory.BLOCKS)
    BlockScheduler.schedule(block = this,
            world = world,
            ticksUntilEnd = CraftStartDuration,
            scheduleId = CrafterPieceBlock.BeginCraftSoundLoop,
            blockPos = pos)
}

fun beginCraftSoundLoop(world: World, pos: BlockPos) {
    if (world.getCrafterEntityOrNull(pos)?.multiblockIn?.isCrafting == true) {
        world.play(CommonPositionedSoundInstance(
                soundEvent = Sounds.CraftLoop,
                category = SoundCategory.BLOCKS,
                pos = pos.toVec3d(),
                repeats = true,
                relative = true
        )
        )
    }

}

// Sometimes we want to stop crafting particles in other ways
fun CrafterMultiblock.stopCraftingWithoutStoppingCraftingParticles(world: World, stopRecipeHelp: Boolean = true) {
    if (stopRecipeHelp) stopRecipeHelpServer(world)
    world.stopSound(Sounds.CraftLoopId, arbitraryCrafterPos.toVec3d())
    world.stopSound(Sounds.CraftStartId, arbitraryCrafterPos.toVec3d())
    cancellationTokens.craftingFinish?.cancel(world)
    cancellationTokens.craftingFinish = null
    cancellationTokens.craftingParticles = null
    if (world.isServer) PlayerStream.watching(world, arbitraryCrafterPos).sendPacket(Packets.StopRecipeHelp(arbitraryCrafterPos))
}

fun CrafterMultiblock.stopCrafting(world: World, stopRecipeHelp: Boolean = true) {
    stopCraftingWithoutStoppingCraftingParticles(world, stopRecipeHelp)
    PlayerStream.watching(world, arbitraryCrafterPos).sendPacket(Packets.StopCraftingParticles(arbitraryCrafterPos))
}


fun CrafterPieceBlock.attemptToFinishCraft(world: World, pos: BlockPos, stopRecipeHelp: Boolean) {
    val multiblock = (world.getBlockEntity(pos) as? CrafterPieceEntity)?.multiblockIn ?: return

    val craftedRecipeOptional = world.recipeManager.getFirstMatch(SpatialRecipe.Type,
            CrafterMultiblockInventoryWrapper(multiblock.getInventory(world), crafterSize = size), world)
    val craftedRecipe = craftedRecipeOptional
            // Can sometimes be null when the player is loading
            .orElse(null) ?: return

    multiblock.finishCraft(world, pos, multiblock, craftedRecipe, stopRecipeHelp)
}


private fun CrafterMultiblock.finishCraft(world: World,
                                          anyCrafterPos: BlockPos,
                                          multiblock: CrafterMultiblock,
                                          craftedRecipe: SpatialRecipe,
                                          stopRecipeHelp: Boolean) {
    stopCrafting(world, stopRecipeHelp = stopRecipeHelp)
    val volume = if (craftedRecipe.craftingEffect == CraftingEffect.particles) 0.4f else 0.1f
    world.play(Sounds.CraftEnd, at = anyCrafterPos, ofCategory = SoundCategory.BLOCKS, volumeMultiplier = volume)

    insertResultToNearbyInventories(world, craftedRecipe, multiblock)


    for (hologram in multiblock.getHologramEntities(world)) {
        hologram.extractItem()
    }

}

private fun CrafterMultiblock.insertResultToNearbyInventories(world: World, craftedRecipe: SpatialRecipe, multiblock: CrafterMultiblock) {
    val attachedInventories = getAttachedInventories(world)

    if (attachedInventories.isEmpty()) {
        world.dropItemStack(craftedRecipe.outputStack, multiblock.centerOfHolograms())
    }
    else {
        world.shootResultToNextInventory(craftedRecipe.outputStack.copy(),
                fromPos = multiblock.centerOfHolograms(),
                inventoryPosIterator = attachedInventories.iterator()
        )
    }
}

fun World.shootResultToNextInventory(stack: ItemStack, fromPos: Vec3d, inventoryPosIterator: Iterator<BlockPos>) {
    val currentPos = inventoryPosIterator.next()
    dropItemStackOnBlock(stack,
            fromPos = fromPos,
            toPos = currentPos) { hitPosition ->
        this.remove()
        val inventory = world.getInventoryIn(currentPos)
        val remainingStack = inventory?.insert(stack) ?: stack.copy()
        // If there is not enough space (remainingStack is not empty),
        // we keep moving the stack to other inventories and try to insert to them.
        if (!remainingStack.isEmpty) {
            if (inventoryPosIterator.hasNext()) {
                shootResultToNextInventory(stack, fromPos = hitPosition, inventoryPosIterator = inventoryPosIterator)
            }
            else {
                dropItemStack(remainingStack, hitPosition)
            }
        }
    }
}


private fun CrafterMultiblock.getAttachedInventories(world: World): List<BlockPos> = MultiblockBoundaries(
        lowCorner = boundaries.lowCorner - BlockPos(1, 0, 1),
        highCorner = boundaries.highCorner + BlockPos(1, 0, 1)
).positionsInHollowBoundaries()
        .filter { world.inventoryExistsIn(it) }

private fun MultiblockBoundaries.positionsInHollowBoundaries(): List<BlockPos> {
    val positions = mutableListOf<BlockPos>()
    assert { lowCorner.y == highCorner.y }
    val y = lowCorner.y
    for (xPos in lowCorner.x..highCorner.x) {
        positions.add(BlockPos(xPos, y, lowCorner.z))
        positions.add(BlockPos(xPos, y, highCorner.z))
    }

    for (zPos in lowCorner.z + 1 until highCorner.z) {
        positions.add(BlockPos(lowCorner.x, y, zPos))
        positions.add(BlockPos(highCorner.x, y, zPos))
    }
    return positions
}


fun CrafterMultiblock.getMatchingRecipes(world: World): List<SpatialRecipe> = world.recipeManager
        .getAllMatches(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(getInventory(world), crafterSize = multiblockSize),
                world)
