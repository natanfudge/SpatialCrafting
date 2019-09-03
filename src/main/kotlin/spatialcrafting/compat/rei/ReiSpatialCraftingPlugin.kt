package spatialcrafting.compat.rei

import me.shedaniel.rei.api.DisplayHelper
import me.shedaniel.rei.api.RecipeDisplay
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import net.fabricmc.loader.api.SemanticVersion
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier
import spatialcrafting.MaxCrafterSize
import spatialcrafting.SmallestCrafterSize
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.modId
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.itemStack
import java.util.function.Function

class ReiSpatialCraftingPlugin : REIPluginV0 {
    companion object {
        val Id = modId("rei_plugin")
        private val CrafterSizes = SmallestCrafterSize..MaxCrafterSize
    }

    override fun getMinimumVersion(): SemanticVersion = SemanticVersion.parse("3.0-pre")

    override fun getPluginIdentifier(): Identifier = Id

    override fun registerPluginCategories(recipeHelper: RecipeHelper) {
        for (i in CrafterSizes) {
            recipeHelper.registerCategory(ReiSpatialCraftingCategory(i))
        }

    }

    private fun <T : Recipe<*>> RecipeHelper.registerRecipes(size: Int, recipeFilter: (Recipe<*>) -> Boolean, mappingFunction: (T) -> RecipeDisplay) {
        registerRecipes(ReiSpatialCraftingCategory.id(size), Function(recipeFilter), Function(mappingFunction))
    }


    override fun registerRecipeDisplays(recipeHelper: RecipeHelper) {
        for (i in CrafterSizes) {
            recipeHelper.registerRecipes<SpatialRecipe>(i,
                    recipeFilter = { recipe -> recipe is SpatialRecipe && recipe.minimumCrafterSize == i },
                    mappingFunction = { ReiSpatialCraftingDisplay(it) }
            )

        }

    }

    override fun registerBounds(displayHelper: DisplayHelper) {
    }

    override fun registerOthers(recipeHelper: RecipeHelper) {
        for (i in CrafterSizes) {
            // Every category gets the crafters from that size onwards
            for (j in i..MaxCrafterSize) {
                recipeHelper.registerWorkingStations(ReiSpatialCraftingCategory.id(i), CraftersPieces.getValue(j).itemStack)

            }

            recipeHelper.removeSpeedCraftButton(ReiSpatialCraftingCategory.id(i))
//            recipeHelper.registerSpeedCraftFunctional()

        }
    }

}


