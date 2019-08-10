package spatialcrafting.compat.rei

import me.shedaniel.rei.api.RecipeDisplay
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier
import spatialcrafting.crafter.CrafterPiece
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.kotlinwrappers.itemStack
import java.util.*

class ReiSpatialCraftingDisplay(val recipe : SpatialRecipe) : RecipeDisplay<SpatialRecipe> {
//    override fun getRecipeCategory(): Identifier = ReiCategory.id(recipe.minimumCrafterSize)
    override fun getRecipeCategory(): Identifier = ReiSpatialCraftingCategory.id(recipe.minimumCrafterSize)

    private val input: List<List<ItemStack>> = recipe.previewComponents.map { listOf(*it.ingredient.stackArray) }
    private val output: List<ItemStack> = listOf(recipe.output)

    var currentLayer = 0

//    fun craftersThatCanCraft() = CraftersPieces.filter { recipe.acceptsCrafterOfSize(it.key) }


    override fun getRecipe(): Optional<Recipe<*>> = Optional.of(recipe)

    override fun getInput(): List<List<ItemStack>> = input
//    + listOf(
//            // Add the available crafter pieces as input so players can see they are used for these recipes
//            craftersThatCanCraft().map {it.value.itemStack }
//    )

    override fun getOutput(): List<ItemStack> = output

    override fun getRequiredItems(): List<List<ItemStack>> = input


}
