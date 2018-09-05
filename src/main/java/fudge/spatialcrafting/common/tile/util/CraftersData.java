package fudge.spatialcrafting.common.tile.util;

import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;


public class CraftersData extends SharedData {

    private static final String CRAFT_TIME_NBT = "craftTime";
    private static final String RECIPE_ID_NBT = "recipeID";
    private static final int NULL_RECIPE = -1;
    private long craftEndTime;
    @Nullable
    private SpatialRecipe currentHelpRecipe;

    public CraftersData(BlockPos pos) {
        super(pos);
    }


    public CraftersData(NBTTagCompound nbt) {
        super(nbt);
    }

    public long getCraftTime() {
        return craftEndTime;
    }

    public void setCraftTime(long lastChangeTime) {
        this.craftEndTime = lastChangeTime;
    }

    @Nullable
    public SpatialRecipe getRecipe() {
        return currentHelpRecipe;
    }

    public void setRecipe(SpatialRecipe currentHelpRecipe) {
        this.currentHelpRecipe = currentHelpRecipe;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        existingData.setLong(CRAFT_TIME_NBT, craftEndTime);
        if (currentHelpRecipe == null) {
            existingData.setInteger(RECIPE_ID_NBT, NULL_RECIPE);
        } else {
            existingData.setInteger(RECIPE_ID_NBT, currentHelpRecipe.getID());
        }
        return existingData;
    }

    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        craftEndTime = serializedData.getLong(CRAFT_TIME_NBT);

        int recipeId = serializedData.getInteger(RECIPE_ID_NBT);
        if (recipeId == NULL_RECIPE) {
            currentHelpRecipe = null;
        } else {
            currentHelpRecipe = SpatialRecipe.fromID(recipeId);
        }
    }


}