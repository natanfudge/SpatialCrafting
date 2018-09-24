package fudge.spatialcrafting.common.util;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class RecipeUtil {

    private RecipeUtil() {}

    /**
     * Checks if an ingredient matches an IItemStack, but return false if the ingredient is null instead of crashing...
     */
    public static boolean nullSafeMatch(@Nullable IIngredient ingredient, @Nullable IItemStack stack) {
        if (ingredient == null) return stack == null;
        boolean matches = ingredient.matches(stack);
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

    public static NBTTagList IIngredientToNbt(IIngredient ingredient) {
        NBTTagList tags = new NBTTagList();

        ingredient.getItems().forEach((crtStack) -> {
            ItemStack stack = CraftTweakerMC.getItemStack(crtStack);
            tags.appendTag(stack.writeToNBT(new NBTTagCompound()));
        });
        return tags;

    }

    @Nullable
    public static IIngredient ingredientFromNbt(NBTTagList serializedData) {
        List<ItemStack> list = new ArrayList<>(serializedData.tagCount());

        for (NBTBase nbtTagElement : serializedData) {
            NBTTagCompound serializedItemStack = (NBTTagCompound) nbtTagElement;
            ItemStack itemStack = new ItemStack(serializedItemStack);
            list.add(itemStack);
        }

        if (list.size() == 0) {
            return null;
        } else if (list.size() == 1) {
            return CraftTweakerMC.getIIngredient(list.get(0));
        } else {
            return getOreDictEntryFromList(list);
        }


    }



    @Nullable
    public static IOreDictEntry getOreDictEntryFromList(List<?> list) {
        for (String ore : OreDictionary.getOreNames()) {
            if (itemStackListEquals(OreDictionary.getOres(ore), list)) {
                return CraftTweakerAPI.oreDict.get(ore);
            }
        }
        return null;
    }

    public static boolean itemStackListEquals(List<ItemStack> list1, List<?> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!itemStackEquals(list1.get(i), list2.get(i))) return false;
        }
        return true;
    }

    public static boolean itemStackEquals(ItemStack stack, Object otherObj) {
        if (!(otherObj instanceof ItemStack)) return false;

        ItemStack other = (ItemStack) otherObj;

        return stack.getItem() == other.getItem() && stack.getCount() == other.getCount() && stack.getMetadata() == other.getMetadata() && stack.getTagCompound() == other.getTagCompound();

    }
}
