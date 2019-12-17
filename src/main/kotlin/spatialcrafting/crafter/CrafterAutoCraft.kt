package spatialcrafting.crafter

import fabricktx.api.copy
import fabricktx.api.isServer
import fabricktx.api.offerOrDrop
import fabricktx.api.sendPacket
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.hologram.getHologramEntity
import spatialcrafting.logWarning
import spatialcrafting.recipe.ComponentSatisfaction
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.recipe.getRecipeSatisfaction

/**
 * - Checks the recipe matches with the player inventory + multiblock inventory
 * - Transfer every missing item of the recipe from the player inventory to the multiblock inventory
 * - Transfer every mismatching item in the multiblock to the player inventory
 */
fun CrafterMultiblock.autoCraft(world: World, withInventoryOfPlayer: PlayerEntity, recipe: SpatialRecipe) {
    assert(world.isServer)
    val (satisfaction, isFullySatisfied) = getRecipeSatisfaction(
            recipe = recipe,
            nearestCrafter = this,
            world = world,
            player = withInventoryOfPlayer
    )
    if (!isFullySatisfied) {
        logWarning { "An autocraft was initiated when the player can't actually craft the recipe with his and the multiblock's inventory." }
        return
    }

    val relativeHologramPositions = hologramsRelativePositions()

    val particlesToSendFromMultiblockToPlayer = mutableListOf<Pair<BlockPos, ItemStack>>()
    val particlesToSendFromPlayerToMultiblock = mutableListOf<Pair<BlockPos, ItemStack>>()

    givePlayerMismatchingItems(world, withInventoryOfPlayer, satisfaction, particlesToSendFromMultiblockToPlayer)

    insertRecipeToMultiblock(satisfaction, world, relativeHologramPositions, particlesToSendFromPlayerToMultiblock)

    PlayerStream.watching(world, arbitraryCrafterPos).sendPacket(Packets.ItemMovementFromPlayerToMultiblockParticles(
            withInventoryOfPlayer.uuid,
            itemsFromMultiblockToPlayer = particlesToSendFromMultiblockToPlayer,
            itemsFromPlayerToMultiblock = particlesToSendFromPlayerToMultiblock
    ))
}



private fun CrafterMultiblock.insertRecipeToMultiblock(satisfaction: List<ComponentSatisfaction>, world: World, relativeHologramPositions: List<AbsoluteAndRelativePos>, particlesToSendFromPlayerToMultiblock: MutableList<Pair<BlockPos, ItemStack>>) {
    for ((componentPos, satisfiedByStack, isAlreadyInMultiblock) in satisfaction) {
        if (!isAlreadyInMultiblock) {
            val hologram = world.getHologramEntity(relativeHologramPositions.first { it.relativePos == componentPos }.absolutePos)
            assert(hologram.isEmpty())
            particlesToSendFromPlayerToMultiblock.add(Pair(hologram.pos, satisfiedByStack!!))
            hologram.insertItem(satisfiedByStack.copy(1))
            satisfiedByStack.count--
        }
    }
}


fun CrafterMultiblock.givePlayerMismatchingItems(world: World, player: PlayerEntity, satisfaction: List<ComponentSatisfaction>,
                                                 particlesToSendFromMultiblockToPlayer: MutableList<Pair<BlockPos, ItemStack>>) {
    for (hologram in getHologramEntities(world)) {
        val item = hologram.getItem()
        if (satisfaction.none { it.satisfiedBy == item }) {
            particlesToSendFromMultiblockToPlayer.add(Pair(hologram.pos, item))
            player.offerOrDrop(hologram.extractItem())
        }
    }
}