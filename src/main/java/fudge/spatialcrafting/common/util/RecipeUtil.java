package fudge.spatialcrafting.common.util;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;

public class RecipeUtil {

    /**
     * Checks if an ingredient matches an IItemStack, but return false if the ingredient is null instead of crashing...
     */
    public static boolean nullSafeMatch(IIngredient ingredient, IItemStack stack) {
        if (ingredient == null) return stack == null;
        return ingredient.matches(stack);
    }
}
