package fudge.spatialcrafting.common.util;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
public class RecipeUtil {

    /**
     * Checks if an ingredient matches an IItemStack, but return false if the ingredient is null instead of crashing...
     */
    public static boolean nullSafeMatch(@Nullable IIngredient ingredient,@Nullable IItemStack stack) {
        if (ingredient == null) return stack == null;
        return ingredient.matches(stack);
    }
}
