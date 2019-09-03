package spatialcrafting.compat.rei

import me.shedaniel.rei.api.RecipeDisplay
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import spatialcrafting.recipe.SpatialRecipe
import java.util.*

class ReiSpatialCraftingDisplay(val recipe : SpatialRecipe) : RecipeDisplay {
//    override fun getRecipeCategory(): Identifier = ReiCategory.id(recipe.minimumCrafterSize)
    override fun getRecipeCategory(): Identifier = ReiSpatialCraftingCategory.id(recipe.minimumCrafterSize)

    private val input: List<List<ItemStack>> = recipe.previewComponents.map { listOf(*it.ingredient.stackArray) }
    private val output: List<ItemStack> = listOf(recipe.outputStack)

    var currentLayer = 0

//    fun craftersThatCanCraft() = CraftersPieces.filter { recipe.acceptsCrafterOfSize(it.key) }

    override fun getRecipeLocation(): Optional<Identifier> = Optional.of(recipe.identifier)

    override fun getInput(): List<List<ItemStack>> = input
//    + listOf(
//            // Add the available crafter pieces as input so players can see they are used for these recipes
//            craftersThatCanCraft().map {it.value.itemStack }
//    )

    override fun getOutput(): List<ItemStack> = output

    override fun getRequiredItems(): List<List<ItemStack>> = input


}
