package spatialcrafting.compat.rei

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeDisplay
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import spatialcrafting.recipe.SpatialRecipe
import java.util.*

class ReiSpatialCraftingDisplay(val recipe : SpatialRecipe) : RecipeDisplay {
    override fun getRecipeCategory(): Identifier = ReiSpatialCraftingCategory.id(recipe.minimumCrafterSize)

    private val input: List<List<ItemStack>> = recipe.previewComponents.map { listOf(*it.ingredient.matchingStacksClient) }
    private val output: List<ItemStack> = listOf(recipe.outputStack)

    var currentLayer = 0

    override fun getRecipeLocation(): Optional<Identifier> = Optional.of(recipe.identifier)

    override fun getInputEntries(): List<List<EntryStack>> = input.map { list -> list.map { EntryStack.create(it) } }

    override fun getOutputEntries(): List<EntryStack> = output.map{EntryStack.create(it)}


    override fun getRequiredEntries() = inputEntries


}
