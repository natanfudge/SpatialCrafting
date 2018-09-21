package fudge.spatialcrafting.common.util;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public final class RecipeUtil {

    private RecipeUtil() {}

    /**
     * Checks if an ingredient matches an IItemStack, but return false if the ingredient is null instead of crashing...
     */
    public static boolean nullSafeMatch(@Nullable IIngredient ingredient, @Nullable IItemStack stack) {
        if (ingredient == null) return stack == null;
        return ingredient.matches(stack);
    }

    /**
     * A workaround to the fact that wildcard metadata itemstacks do not have a model
     */
    public static ItemStack getVisibleItemStack(@Nullable IIngredient ingredient) {
        ItemStack stack = CraftTweakerMC.getItemStack(ingredient);

        if (stack.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            return stack;
        } else {
            return new ItemStack(stack.getItem());
        }
    }
}
