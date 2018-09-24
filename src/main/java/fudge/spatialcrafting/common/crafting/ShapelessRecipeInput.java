package fudge.spatialcrafting.common.crafting;

import com.google.common.collect.ImmutableList;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import fudge.spatialcrafting.common.tile.util.CraftingInventory;
import fudge.spatialcrafting.common.tile.util.CubeArr;
import fudge.spatialcrafting.common.util.RecipeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static fudge.spatialcrafting.common.crafting.RecipeInputSerialization.RECIPE_INPUT_NBT;
import static fudge.spatialcrafting.common.crafting.RecipeInputSerialization.SHAPED_NBT;

public class ShapelessRecipeInput implements IRecipeInput {

    private final List<IIngredient> ingredients;
    private final int layerSize;

    public ShapelessRecipeInput(IIngredient[] ingredients) {
        this(Arrays.asList(ingredients));
    }

    public ShapelessRecipeInput(List<IIngredient> ingredients) {
        this.ingredients = ingredients;
        layerSize = Math.max(2, ceil(Math.cbrt(ingredients.size())));
    }

    private static int ceil(double num) {
        return (int) num == num ? (int) num : (int) num + 1;
    }

    public static IRecipeInput fromNBT(NBTTagCompound serializedData) {
        NBTTagList recipeInputNbt = serializedData.getTagList(RECIPE_INPUT_NBT, Constants.NBT.TAG_LIST);
        IIngredient[] ingredients = new IIngredient[recipeInputNbt.tagCount()];

        for (int i = 0; i < recipeInputNbt.tagCount(); i++) {
            IIngredient ingredient = RecipeUtil.ingredientFromNbt((NBTTagList) recipeInputNbt.get(i));
            if (ingredient != null) {
                ingredients[i] = ingredient;
            }
        }

        return new ShapelessRecipeInput(ingredients);
    }

    List<IIngredient> getIngredients() {
        return ingredients;
    }

    @Override
    @Nullable
    public IIngredient get(int height, int row, int col) {
        int pos = col + row * layerSize + height * layerSize * layerSize;
        if (pos < ingredients.size()) {
            return ingredients.get(pos);
        } else {
            return null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {

        NBTTagList nbtList = new NBTTagList();
        for (IIngredient ingredient : ingredients) {
            if (ingredient == null) {
                nbtList.appendTag(new NBTTagList());
            } else {
                nbtList.appendTag(RecipeUtil.IIngredientToNbt(ingredient));
            }
        }

        existingData.setTag(RECIPE_INPUT_NBT, nbtList);
        existingData.setBoolean(SHAPED_NBT, false);

        return existingData;
    }

    @Override
    public String toFormattedString() {
        final String NULL = "      null      ";

        StringBuilder builder = new StringBuilder("[");

        for (IIngredient ingredient : ingredients) {
            builder.append(ingredient == null ? NULL : ingredient);
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append("]");

        return builder.toString();
    }

    @Override
    public List<List<ItemStack>> itemStackOfLayer(int layer) {
        List<List<ItemStack>> list = new ArrayList<>();

        for (int i = layer * layerSize * layerSize; i < (layer + 1) * layerSize * layerSize; i++) {

            if (i >= ingredientAmount()) {
                list.add(ImmutableList.of(ItemStack.EMPTY));
                continue;
            }

            IIngredient ingredient = ingredients.get(i);
            if (ingredient != null) {
                list.add(Arrays.asList(CraftTweakerMC.getItemStacks(ingredient.getItems())));
            } else {
                list.add(ImmutableList.of(ItemStack.EMPTY));
            }
        }

        return list;

    }

    //FIXME flowers turn into dead bushes
    @Override
    public int ingredientAmount() {
        return ingredients.size();
    }

    @Override
    public int layerSize() {
        return layerSize;
    }

    @Override
    public boolean intersectsWith(IRecipeInput other) {
        Collection<IIngredient> ingredientContainer;

        if (this.ingredientAmount() != other.ingredientAmount()) return false;

        if (other instanceof ShapelessRecipeInput) {
            ingredientContainer = ((ShapelessRecipeInput) other).ingredients;
        } else if (other instanceof ShapedRecipeInput) {
            ingredientContainer = (Collection<IIngredient>) other;
        } else {
            throw new UnsupportedOperationException("Unexpected IRecipeInput subclass!");
        }

        List<IIngredient> ingredientPool = new ArrayList<>(this.ingredients);

        for (IIngredient otherIngr : ingredientContainer) {
            if (otherIngr == null) continue;
            IIngredient correspondingIngredient = null;
            for (IIngredient thisIngr : ingredientPool) {
                if (thisIngr.contains(otherIngr) || otherIngr.contains(thisIngr)) {
                    correspondingIngredient = thisIngr;
                    break;
                }

            }

            if (correspondingIngredient == null) {
                return false;
            } else {
                ingredientPool.remove(correspondingIngredient);
            }


        }


        // There is no ingredient that is different enough from another one, so the recipes intersect.
        return true;


    }

    @Override
    public boolean isEquivalentTo(IRecipeInput other) {
        return other instanceof ShapelessRecipeInput && this.ingredients.equals(((ShapelessRecipeInput) other).ingredients);
    }

    @Override
    public boolean matches(CraftingInventory inventory) {
        if (ingredientAmount() != inventory.getStackAmount()) return false;

        // Go through every ingredient and see if the crafting inventory contains something that matches it.
        for (IIngredient ingredient : ingredients) {
            boolean found = false;
            for (IItemStack stack : inventory.toIItemStackArr()) {
                if (ingredient.matches(stack)) {
                    found = true;
                    break;
                }
            }

            if (!found) return false;
        }
        return true;
    }

    @Override
    public boolean matchesLayer(CraftingInventory inventory, int layer, boolean[][] hologramsActive) {

        CubeArr<IItemStack> iItemStacks = inventory.toIItemStackArr();

        for (int i = layer * layerSize * layerSize; i < Math.min((layer + 1) * layerSize * layerSize, ingredientAmount()); i++) {
            boolean found = false;
            inventorySearch:
            for (int j = 0; j < inventory.getCubeSize(); j++) {
                for (int k = 0; k < inventory.getCubeSize(); k++) {
                    IItemStack stack = iItemStacks.get(layer, j, k);
                    if (ingredients.get(i).matches(stack)) {
                        found = true;
                        // Prevent one stack serving as many
                        iItemStacks.set(layer, j, k, null);
                        break inventorySearch;
                    }
                }
            }
            if (!found) return false;
        }

        return true;

    }


    @Override
    public ItemStack getCorrespondingStack(CraftingInventory inventory, CubeArr<IItemStack> iItemStacks, int i, int j, int k) {
        IIngredient ingredient = this.get(i, j, k);
        if (ingredient == null) {
            return ItemStack.EMPTY;
        } else {
            int size = iItemStacks.getCubeSize();
            for (int height = 0; height < size; height++) {
                for (int row = 0; row < size; row++) {
                    for (int col = 0; col < size; col++) {
                        IItemStack stack = iItemStacks.get(height, row, col);
                        if (ingredient.matches(stack)) {
                            iItemStacks.set(height, row, col, null);
                            return CraftTweakerMC.getItemStack(stack);
                        }
                    }
                }
            }

            throw new RuntimeException("This shouldn't be possible!");
        }

    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ShapelessRecipeInput)) return false;

        return this.ingredients.equals(((ShapelessRecipeInput) other).ingredients);
    }

    @Override
    public int hashCode() {
        return ingredients.hashCode();
    }
}
