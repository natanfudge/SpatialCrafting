package fudge.spatialcrafting.compat.jei;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.block.SCBlocks;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


@JEIPlugin
public class ScJeiPlugin implements IModPlugin {

    private static List<CategorySpatialRecipe> recipeCategories = new ArrayList<>(4);

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        // No subtypes
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        // No ingredients
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (recipeCategories.isEmpty()) {
            for (int i = 2; i <= 5; i++) {
                recipeCategories.add(new CategorySpatialRecipe(registry.getJeiHelpers().getGuiHelper(), i));
            }
        }

        recipeCategories.forEach(registry::addRecipeCategories);


    }

    @Override
    public void register(IModRegistry registry) {
        for (int i = 2; i <= 5; i++) {
            int finali = i;
            String UID = SpatialCrafting.MODID + i;
            registry.handleRecipes(SpatialRecipe.class, (recipe -> new WrapperSpatialRecipe(recipe, recipeCategories.get(finali - 2))), UID);
            registry.addRecipes(SpatialRecipe.getRecipesForSize(i), UID);

            List<BlockCrafter> crafters = SCBlocks.getAllCrafterBlocks();
            registry.addRecipeCatalyst(new ItemStack(crafters.get(i - 2)), UID);


        }


    }


    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // No runtime actions needed
    }

}
