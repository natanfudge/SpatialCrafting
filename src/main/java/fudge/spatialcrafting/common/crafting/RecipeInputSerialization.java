package fudge.spatialcrafting.common.crafting;

import net.minecraft.nbt.NBTTagCompound;

public final class RecipeInputSerialization {

    public static final String RECIPE_INPUT_NBT = "recipeInput";
    public static final String SHAPED_NBT = "shaped";

    private RecipeInputSerialization() {}

    static IRecipeInput fromNBT(NBTTagCompound serializedData) {
        boolean shaped = serializedData.getBoolean(SHAPED_NBT);
        if (shaped) {
            return ShapedRecipeInput.Companion.fromNBT(serializedData);
        } else {
            return ShapelessRecipeInput.fromNBT(serializedData);
        }


    }

}
