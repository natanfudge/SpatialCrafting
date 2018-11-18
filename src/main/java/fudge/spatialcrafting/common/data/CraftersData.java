package fudge.spatialcrafting.common.data;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;


/**
 * Warning: do not initialize variables at constructor time, it will overwrite the existing info since reading info happens at the superclass.
 */
public class CraftersData extends SharedData {

    private static class NBT {
        private static final String CRAFT_TIME = "craftTime";
        private static final String RECIPE_ID = "recipeID";
        private static final String ACTIVE_LAYER = "activeLayer";
        private static final String PLAYER = "player";
        private static final String ENERGY = "energy";
        private static final String SIZE = "size";
    }


    private static final int NULL_RECIPE = -1;
    private static final UUID NULL_PLAYER = new UUID(0xFFFFFFFF_FFFFFFFFL, 0xFFFFFFFF_FFFFFFFFL);
    private static final List<Integer> MAX_ENERGY = ImmutableList.of(20000, 30000, 40000, 50000);
    private static final List<Integer> RECIEVE_SPEED = ImmutableList.of(50, 100, 200, 400);


    private long craftEndTime;

    public IEnergyStorage getEnergyHandler() {
        return energyHandler;
    }

    private byte activeLayer;
    @Nullable
    private UUID craftingPlayer;
    private IEnergyStorage energyHandler;

    @Nullable
    private SpatialRecipe currentHelpRecipe;
    private int size;

    public int getSize() {
        return size;
    }

    public CraftersData(BlockPos pos, int size) {
        super(pos);
        this.size = size;
        int maxEnergy = MAX_ENERGY.get(size - 2);
        this.energyHandler = new CraftersEnergy(maxEnergy, RECIEVE_SPEED.get(size - 2),maxEnergy , 0) {
            @Override
            public void onEnergyChanged() {
                markDirty();
            }
        };
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
        markDirty();
    }

    public long getCraftTime() {
        return craftEndTime;
    }

    public void setCraftTime(long lastChangeTime) {
        this.craftEndTime = lastChangeTime;
        markDirty();
    }

    public byte getActiveLayer() {
        return activeLayer;
    }

    public void setActiveLayer(byte activeLayer) {
        this.activeLayer = activeLayer;
        markDirty();
    }

    @Nullable
    public SpatialRecipe getRecipe() {
        return currentHelpRecipe;
    }

    public void setRecipe(@Nullable SpatialRecipe helpRecipe) {
        this.currentHelpRecipe = helpRecipe;
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {

        existingData.setInteger(NBT.ENERGY, energyHandler.getEnergyStored());
        existingData.setInteger(NBT.SIZE, size);
        existingData.setLong(NBT.ACTIVE_LAYER, activeLayer);
        existingData.setLong(NBT.CRAFT_TIME, craftEndTime);

        if (currentHelpRecipe == null) {
            existingData.setInteger(NBT.RECIPE_ID, NULL_RECIPE);
        } else {
            existingData.setInteger(NBT.RECIPE_ID, currentHelpRecipe.getID());
        }
        if (craftingPlayer != null) {
            existingData.setUniqueId(NBT.PLAYER, craftingPlayer);
        } else {
            existingData.setUniqueId(NBT.RECIPE_ID, NULL_PLAYER);
        }


        return existingData;
    }

    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        craftEndTime = serializedData.getLong(NBT.CRAFT_TIME);
        activeLayer = serializedData.getByte(NBT.ACTIVE_LAYER);
        size = serializedData.getInteger(NBT.SIZE);

        int maxEnergy = MAX_ENERGY.get(size - 2);

        energyHandler = new CraftersEnergy(maxEnergy,
                RECIEVE_SPEED.get(size - 2),
                maxEnergy,
                serializedData.getInteger(NBT.ENERGY)) {
            @Override
            public void onEnergyChanged() {
                markDirty();
            }
        };
        int recipeId = serializedData.getInteger(NBT.RECIPE_ID);
        if (recipeId == NULL_RECIPE) {
            currentHelpRecipe = null;
        } else {
            currentHelpRecipe = SpatialRecipe.fromID(recipeId);
        }


        UUID playerUuid = serializedData.getUniqueId(NBT.PLAYER);
        if (playerUuid == NULL_PLAYER) {
            craftingPlayer = null;
        } else {
            craftingPlayer = playerUuid;
        }
    }


}
