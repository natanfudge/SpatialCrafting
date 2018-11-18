package fudge.spatialcrafting.common.data;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketRemoveMasterBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WorldSavedDataCrafters extends WorldSavedData {

    private static final String DATA_NBT = "data";
    private static final String DATA_NAME = SpatialCrafting.MODID + " worldSavedData";
    private List<SharedData> allSharedData = new ArrayList<>();

    // Required constructor
    public WorldSavedDataCrafters(String name) {
        super(name);
    }

    public WorldSavedDataCrafters() {
        this(DATA_NAME);
    }

    /**
     * For client-side syncing
     *
     * @param world should be a client world
     * @param data  the data transferred from the packet
     */
    public static void setAllData(World world, List<SharedData> data) {
        getInstance(world).allSharedData = data;
    }

    /**
     *
     */
    public static List<SharedData> getAllData(World world) {
        return getInstance(world).allSharedData;
    }

    /**
     * Returns the WorldSavedDataCrafters instance of a world
     */
    private static WorldSavedDataCrafters getInstance(World world) {
        //Every World object has a respective instance of this class (WorldSavedDataCrafters) stored inside it.
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataCrafters instance = (WorldSavedDataCrafters) storage.getOrLoadData(WorldSavedDataCrafters.class, DATA_NAME);

        // Initially, that instance is null so the first time we access that instance we replace it with an actual WorldSavedDataCrafters.
        if (instance == null) {
            instance = new WorldSavedDataCrafters();
            storage.setData(DATA_NAME, instance);
        }

        return instance;
    }


    public static List<BlockPos> getMasterBlocks(World world) {
        List<BlockPos> masterBlocks = new LinkedList<>();

        getAllData(world).forEach(data -> masterBlocks.add(data.getMasterPos()));

        return masterBlocks;
    }

    public static void addData(World world, BlockPos pos, int crafterSize) {
        WorldSavedDataCrafters instance = getInstance(world);
        // For some reason allSharedData becomes null sometimes, this fixes it but it shouldn't be like this...
        if (instance.allSharedData == null) instance.allSharedData = new ArrayList<>();
        SharedData data = new CraftersData(pos, crafterSize);
        instance.allSharedData.add(data);

        instance.markDirty();
    }

    public static void removeData(World world, BlockPos pos, boolean syncClient) {
        WorldSavedDataCrafters instance = getInstance(world);
        instance.allSharedData.removeIf(data -> data.getMasterPos().equals(pos));
        instance.markDirty();

        if (syncClient) {
            PacketHandler.getNetwork().sendToDimension(new PacketRemoveMasterBlock(pos), world.provider.getDimension());
        }
    }

    // In the future might be better to return a "CraftersData" object
    @Nullable
    public static SharedData getDataForMasterPos(@Nonnull World world, @Nonnull BlockPos pos) {
        List<SharedData> allData = getAllData(world);
        for (SharedData data : allData) {
            if (data.getMasterPos().equals(pos)) return data;
        }

        return null;

    }

    public static void setOneData(World world, SharedData dataToSet) {
        WorldSavedDataCrafters instance = getInstance(world);
        instance.allSharedData.replaceAll(data -> {
            if (data.getMasterPos().equals(dataToSet.getMasterPos())) {
                return dataToSet;
            } else {
                return data;
            }
        });
        instance.markDirty();
    }

    private NBTTagCompound serialized(NBTTagCompound existingData) {

        NBTTagList list = new NBTTagList();
        allSharedData.forEach(data -> list.appendTag(data.serialized(new NBTTagCompound())));

        existingData.setTag(DATA_NBT, list);

        return existingData;
    }

    private void deserialize(NBTTagCompound serializedData) {
        NBTTagList list = serializedData.getTagList(DATA_NBT, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            // Hardcoded for CraftersData
            allSharedData.add(new CraftersData(list.getCompoundTagAt(i)));
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound existingData) {
        return this.serialized(existingData);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound serializedData) {
        deserialize(serializedData);
    }



    @Override
    public boolean isDirty() {
        boolean anyDirty = false;
        for (SharedData data : allSharedData) {
            if (data.isDirty()) {
                anyDirty = true;
                data.setDirty(false);
            }
        }
        return anyDirty || super.isDirty();
    }


}


