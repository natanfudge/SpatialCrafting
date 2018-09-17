package fudge.spatialcrafting.compat.jei;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.block.SCBlocks;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.startup.ModRegistry;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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

    //TODO make adding recipes update jei

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

        // Cache for later use
        MOD_REGISTRY = registry;


    }

    public static void addRecipe(SpatialRecipe recipe){
        MOD_REGISTRY.addRecipes(Collections.singletonList(recipe), SpatialCrafting.MODID + recipe.size());
    }

    public static RecipesGui JEI_GUI;
    public static IModRegistry MOD_REGISTRY;


    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // Kidnap gui for nefarius uses
        JEI_GUI = (RecipesGui) jeiRuntime.getRecipesGui();




    }

}
