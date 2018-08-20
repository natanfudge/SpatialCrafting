package fudge.spatialcrafting.common.data;

import fudge.spatialcrafting.SpatialCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldSavedDataCrafters extends WorldSavedData {

    public static final String CRAFTERS_NBT = "crafters";
    private static final String DATA_NAME = SpatialCrafting.MODID + " worldSavedData";
    private Map<BlockPos, Long> craftEndTimes;

    // Required constructor
    public WorldSavedDataCrafters(String name) {
        super(name);
        craftEndTimes = new HashMap<>();
    }

    public WorldSavedDataCrafters() {
        this(DATA_NAME);
    }

    /**
     * For client-side syncing
     *
     * @param world should be a client world
     * @param data  the data transferred from the packet (a map)
     */
    public static void setData(World world, Map<BlockPos, Long> data) {
        getInstance(world).craftEndTimes = data;
    }

    /**
     * For client-side syncing
     *
     * @param world should be a server world
     */
    public static Map<BlockPos, Long> getData(World world) {
        return getInstance(world).craftEndTimes;
    }

    /**
     * Returns the WorldSavedDataCrafters instance of a world
     */
    public static WorldSavedDataCrafters getInstance(World world) {
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

    public static Set<BlockPos> getMasterBlocks(World world) {
        return getInstance(world).craftEndTimes.keySet();
    }

    public static void addMasterBlock(World world, BlockPos pos) {
        WorldSavedDataCrafters instance = getInstance(world);
        instance.craftEndTimes.put(pos, 0L);
        instance.markDirty();
    }

    public static void removeMasterBlock(World world, BlockPos pos) {
        WorldSavedDataCrafters instance = getInstance(world);
        instance.craftEndTimes.remove(pos);
        instance.markDirty();
    }

    // In the future might be better to return a "SharedData" object
    public static long getDataForMasterPos(@Nonnull World world, @Nonnull BlockPos pos) {
        Map<BlockPos, Long> times = getInstance(world).craftEndTimes;
        if (times.containsKey(pos)) {
            return times.get(pos);
        } else {
            return 0;
        }

    }

    // In the future might be better to return a "SharedData" object
    public static void setDataForMasterPos(World world, BlockPos pos, long craftEndTime) {
        WorldSavedDataCrafters instance = getInstance(world);
        instance.craftEndTimes.replace(pos, craftEndTime);
        instance.markDirty();
    }

    public NBTTagCompound serialized(NBTTagCompound existingData) {
        NBTTagCompound craftersNBT = new NBTTagCompound();
        craftEndTimes.forEach((pos, time) -> craftersNBT.setLong(Long.toString(pos.toLong()), time));

        existingData.setTag(CRAFTERS_NBT, craftersNBT);
        return existingData;
    }

    public void deserialize(NBTTagCompound serializedData) {
        NBTTagCompound craftersNBT = serializedData.getCompoundTag(CRAFTERS_NBT);
        Set<String> keys = craftersNBT.getKeySet();

        keys.forEach(key -> craftEndTimes.put(BlockPos.fromLong(Long.parseLong(key)), Long.parseLong(key)));

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        return this.serialized(existingData);
    }

    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        deserialize(serializedData);

    }

}


