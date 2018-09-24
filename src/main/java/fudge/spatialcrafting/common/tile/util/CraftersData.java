package fudge.spatialcrafting.common.tile.util;

import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;


public class CraftersData extends SharedData {

    private static final String CRAFT_TIME_NBT = "craftTime";
    private static final String RECIPE_ID_NBT = "recipeID";
    private static final String ACTIVE_LAYER_NBT = "activeLayer";
    private static final String PLAYER_NBT = "player";
    private static final int NULL_RECIPE = -1;
    private static final UUID NULL_PLAYER = new UUID(0xFFFFFFFF_FFFFFFFFL, 0xFFFFFFFF_FFFFFFFFL);

    private long craftEndTime;
    private byte activeLayer;
    @Nullable
    private UUID craftingPlayer;

    @Nullable
    private SpatialRecipe currentHelpRecipe;

    public CraftersData(BlockPos pos) {
        super(pos);
    }

    public CraftersData(NBTTagCompound nbt) {
        super(nbt);
    }

    @Nullable
    public UUID getCraftingPlayer() {
        return craftingPlayer;
    }

    public void setCraftingPlayer(@Nullable UUID craftingPlayer) {
        this.craftingPlayer = craftingPlayer;
    }

    public long getCraftTime() {
        return craftEndTime;
    }

    public void setCraftTime(long lastChangeTime) {
        this.craftEndTime = lastChangeTime;
    }

    public byte getActiveLayer() {
        return activeLayer;
    }

    public void setActiveLayer(byte activeLayer) {
        this.activeLayer = activeLayer;
    }

    @Nullable
    public SpatialRecipe getRecipe() {
        return currentHelpRecipe;
    }

    public void setRecipe(@Nullable SpatialRecipe helpRecipe) {
        this.currentHelpRecipe = helpRecipe;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        existingData.setLong(CRAFT_TIME_NBT, craftEndTime);
        if (currentHelpRecipe == null) {
            existingData.setInteger(RECIPE_ID_NBT, NULL_RECIPE);
        } else {
            existingData.setInteger(RECIPE_ID_NBT, currentHelpRecipe.getID());
        }
        existingData.setLong(ACTIVE_LAYER_NBT, activeLayer);
        if (craftingPlayer != null) {
            existingData.setUniqueId(PLAYER_NBT, craftingPlayer);
        } else {
            existingData.setUniqueId(RECIPE_ID_NBT, NULL_PLAYER);
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

        activeLayer = serializedData.getByte(ACTIVE_LAYER_NBT);

        UUID playerUuid = serializedData.getUniqueId(PLAYER_NBT);
        if (playerUuid == NULL_PLAYER) {
            craftingPlayer = null;
        } else {
            craftingPlayer = playerUuid;
        }
    }


}
