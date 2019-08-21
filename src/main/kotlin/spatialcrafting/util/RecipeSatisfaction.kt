package spatialcrafting.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.recipe.ComponentPosition
import spatialcrafting.recipe.SpatialRecipe

 fun getRecipeSatisfaction(recipe: SpatialRecipe, nearestCrafter: CrafterMultiblock, world: World, player: PlayerEntity): RecipeSatisfaction {
    val crafterInventory = nearestCrafter.getInventory(world)

    var fullySatisfied = true
    val playerItems = player.itemsInInventoryAndOffhand

    // Tracks how many ingredients an itemstack in the player's inventory satisfies.
    // This is so one itemstack that holds 2 of the item doesn't satisfy 10 different ingredients
    val satisfactionMap = mutableMapOf<ItemStack, Int>()

    val satisfaction = recipe.previewComponents.map { (pos, ingredient) ->
        // Check if the ingredient exists in the multiblock already
        val stackInMultiblockAtPos = crafterInventory.find { it.position == pos }?.itemStack
                ?: ItemStack.EMPTY

        var satisfiedBy: ItemStack? = null
        var isAlreadyInMultiblock = false

        if (ingredient.matches(stackInMultiblockAtPos)) {
            satisfiedBy = stackInMultiblockAtPos
            isAlreadyInMultiblock = true
        }
        else {
            for (playerStack in playerItems) {
                if (!ingredient.matches(playerStack)) continue
                val ingredientsSatisfiedByStack = satisfactionMap[playerStack] ?: 0
                // Check that a single stack of 2 items does't satisfy more than 2
                if (playerStack.count > ingredientsSatisfiedByStack) {
                    satisfiedBy = playerStack
                    satisfactionMap[playerStack] = ingredientsSatisfiedByStack + 1
                    break
                }
            }
        }

        if (satisfiedBy == null) fullySatisfied = false

        return@map ComponentSatisfaction(pos, satisfiedBy,isAlreadyInMultiblock)
    }

    return RecipeSatisfaction(satisfaction, fullySatisfied)
}


data class ComponentSatisfaction(val pos: ComponentPosition, val satisfiedBy: ItemStack?, val isAlreadyInMultiblock : Boolean)
data class RecipeSatisfaction(val componentSatisfaction: List<ComponentSatisfaction>, val fullySatisfied: Boolean)