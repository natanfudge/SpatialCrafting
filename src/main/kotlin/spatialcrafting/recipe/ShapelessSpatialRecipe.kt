package spatialcrafting.recipe

import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.world.World
import spatialcrafting.client.Duration
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.util.matches

class ShapelessSpatialRecipe(private val components: List<ShapelessRecipeComponent>,
                             minimumCrafterSize: Int,
                             energyCost: Long,
                             craftTime: Duration,
                             output: ItemStack,
                             id: Identifier) : SpatialRecipe(output, id, minimumCrafterSize, energyCost, craftTime) {
    override fun getSerializer() = Serializer

    data class ItemAndAmount(val stack: ItemStack, val amount: Int)

    override fun matches(inventory: CrafterMultiblockInventoryWrapper, world: World): Boolean {
        val shapelessInventory = inventory.groupBy { it.itemStack.item }.map { ItemAndAmount(it.value[0].itemStack, it.value.size) }
        if (shapelessInventory.size != components.size) return false
        if (minimumCrafterSize > inventory.crafterSize) return false

        return components.all { component ->
            // Check every component exists in the inventory with the required amount
            shapelessInventory.any { component.ingredient.matches(it.stack) && component.amount == it.amount }
        }

    }

    companion object Serializer : SpatialRecipe.Serializer<ShapelessSpatialRecipe>() {
        override fun build(components: List<ShapedRecipeComponent>, id: Identifier, output: ItemStack,
                           minimumCrafterSize: Int, energyCost: Long, craftTime: Duration): ShapelessSpatialRecipe {

            // We only care about the ingredients and the amount of each one. Position has no meaning.
            val shapelessComponents = components.groupBy { it.ingredient.ids }
                    .map { ShapelessRecipeComponent(it.value[0].ingredient, it.value.size) }

            return ShapelessSpatialRecipe(
                    components = shapelessComponents,
                    minimumCrafterSize = minimumCrafterSize,
                    output = output,
                    id = id,
                    energyCost = energyCost,
                    craftTime = craftTime
            )
        }


    }

}

data class ShapelessRecipeComponent(val ingredient: Ingredient, val amount: Int)