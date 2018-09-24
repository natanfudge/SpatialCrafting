package fudge.spatialcrafting.common.crafting;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import fudge.spatialcrafting.common.tile.util.CraftingInventory;
import fudge.spatialcrafting.common.tile.util.CubeArr;
import fudge.spatialcrafting.common.tile.util.Offset;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public interface IRecipeInput {

    @Nullable
    IIngredient get(int height, int row, int col);


    NBTTagCompound writeToNBT(NBTTagCompound existingData);

    String toFormattedString();

    List<List<ItemStack>> itemStackOfLayer(int layer);

    @Nullable
    // Note that it's [y,x,z] and not [x,y,z]!
    default IIngredient get(Offset offset){
        return get(offset.getY(), offset.getX(), offset.getZ());
    }

    int ingredientAmount();

    int layerSize();

    boolean intersectsWith(IRecipeInput other);

    boolean isEquivalentTo(IRecipeInput other);

    boolean matches(CraftingInventory inventory);

    boolean matchesLayer(CraftingInventory inventory, int layer,boolean[][] hologramsActive);

    ItemStack getCorrespondingStack(CraftingInventory inventory, CubeArr<IItemStack> iItemStacks, int i, int j, int k);


    String RECIPE_INPUT_NBT = "recipeInput";
    String SHAPED_NBT = "shaped";


    static IRecipeInput fromNBT(NBTTagCompound serializedData) {
        boolean shaped = serializedData.getBoolean(SHAPED_NBT);
        if (shaped) {
            return ShapedRecipeInput.Companion.fromNBT(serializedData);
        } else {
            return ShapelessRecipeInput.fromNBT(serializedData);
        }


    }


}
