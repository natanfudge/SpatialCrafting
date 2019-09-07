package spatialcrafting.crafter

import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.client.Sounds
import spatialcrafting.client.particle.centerOfHolograms
import spatialcrafting.client.particle.toVec3d
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.ticker.Scheduler
import spatialcrafting.util.*

fun CrafterPieceBlock.craft(matches: List<SpatialRecipe>, world: World, multiblockIn: CrafterMultiblock, pos: BlockPos) {
    assert(world.isServer)
    if (matches.size > 1) {
        logWarning {
            "THERE IS MORE THAN ONE RECIPE THAT MATCHES THE SAME INPUT!\n" +
                    "ONLY THE FIRST RECIPE WILL BE USED! The recipes are: \n$matches"
        }
    }

    val craftDuration = matches[0].craftTime
    val endTime = world.durationTime + craftDuration

    multiblockIn.setIsCrafting(craftEndTime = endTime)
    playCraftingSounds(world, multiblockIn.arbitraryCrafterPos())

    PlayerStream.watching(world.getBlockEntity(pos)).sendPacket(
            Packets.StartCraftingParticles(multiblockIn.arbitraryCrafterPos(), craftDuration)
    )

    multiblockIn.showHologramsWithItemOnly(world)

    multiblockIn.cancellationTokens.craftingFinish = Scheduler.schedule(block = this,
            world = world,
            ticksUntilEnd = craftDuration.inTicks.toInt(),
            blockPos = pos,
            scheduleId = CrafterPieceBlock.FinishCraft)


    logDebug {
        "Scheduling craft at ${world.time} scheduled to end at $endTime"
    }
}

private const val CraftStartDuration = (1.936281f * TicksPerSecond).toInt()
//private const val CraftLoopDuration = (1.379478 / 2 * TicksPerSecond).toInt()

//TODO: stop sound when crafting ends
private fun CrafterPieceBlock.playCraftingSounds(world: World, pos: BlockPos) {
    assert(world.isServer)
    world.play(Sounds.CraftStart, at = pos, ofCategory = SoundCategory.BLOCKS)
    Scheduler.schedule(block = this,
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
fun CrafterMultiblock.stopCraftingWithoutStoppingCraftingParticlesHere(world: World) {
    stopRecipeHelpServer(world)
    setNotCrafting()
    world.stopSound(Sounds.CraftLoopId, arbitraryCrafterPos().toVec3d())
    cancellationTokens.craftingFinish?.cancel(world)
}

fun CrafterMultiblock.stopCrafting(world: World) {
    stopCraftingWithoutStoppingCraftingParticlesHere(world)
    PlayerStream.watching(world, arbitraryCrafterPos()).sendPacket(Packets.StopCraftingParticles(arbitraryCrafterPos()))
}


fun CrafterPieceBlock.attemptToFinishCraft(world: World, pos: BlockPos) {
    val multiblock = (world.getBlockEntity(pos) as? CrafterPieceEntity)?.multiblockIn ?: return

    val craftedRecipeOptional = world.recipeManager.getFirstMatch(SpatialRecipe.Type,
            CrafterMultiblockInventoryWrapper(multiblock.getInventory(world), crafterSize = size), world)
    val craftedRecipe = craftedRecipeOptional
            // Can sometimes be null when the player is loading
            .orElse(null) ?: return

    finishCraft(world, pos, multiblock, craftedRecipe)
}


private fun finishCraft(world: World, anyCrafterPos: BlockPos, multiblock: CrafterMultiblock, craftedRecipe: SpatialRecipe) {
    world.stopSound(Sounds.CraftLoopId, anyCrafterPos.toVec3d())
    world.play(Sounds.CraftEnd, at = anyCrafterPos, ofCategory = SoundCategory.BLOCKS, volumeMultiplier = 0.4f)


    multiblock.setNotCrafting()

    world.dropItemStack(craftedRecipe.outputStack, multiblock.centerOfHolograms().toBlockPos())

    for (hologram in multiblock.getHologramEntities(world)) {
        hologram.extractItem()
    }


    multiblock.stopRecipeHelpServer(world)
    PlayerStream.watching(world, anyCrafterPos).sendPacket(Packets.StopRecipeHelp(anyCrafterPos))
}

fun CrafterMultiblock.getMatchingRecipes(world: World): List<SpatialRecipe> = world.recipeManager
        .getAllMatches(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(getInventory(world), crafterSize = multiblockSize),
                world)
