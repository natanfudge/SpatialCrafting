package fudge.spatialcrafting.common.data;

import fudge.spatialcrafting.SpatialCrafting;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.LinkedList;
import java.util.List;

public class SCWorldSavedData extends WorldSavedData {

    private static final String DATA_NAME = SpatialCrafting.MODID + " worldSavedData";
    private final List<BlockPos> masterBlocks = new LinkedList<>();

    // Required constructor
    public SCWorldSavedData(String name) {
        super(name);
    }

    public SCWorldSavedData() {
        super(DATA_NAME);
    }


    /**
     * Returns the SCWorldSavedData instance of a world
     */
    public static SCWorldSavedData getInstance(World world) {
        //Every World object has a respective instance of this class (SCWorldSavedData) stored inside it.
        MapStorage storage = world.getPerWorldStorage();
        SCWorldSavedData instance = (SCWorldSavedData) storage.getOrLoadData(SCWorldSavedData.class, DATA_NAME);

        // Initially, that instance is null so the first time we access that instance we replace it with an actual SCWorldSavedData.
        if (instance == null) {
            instance = new SCWorldSavedData();
            storage.setData(DATA_NAME, instance);
        }

        return instance;
    }

    public static List<BlockPos> getMasterBlocks(World world) {
        return getInstance(world).masterBlocks;
    }

    public static void addMasterBlock(World world, BlockPos pos) {
        SCWorldSavedData instance = getInstance(world);
        instance.masterBlocks.add(pos);
        instance.markDirty();
    }

    // Helper methods

    public static void removeMasterBlock(World world, BlockPos pos) {
        SCWorldSavedData instance = getInstance(world);
        instance.masterBlocks.remove(pos);
        instance.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList craftersNBT = new NBTTagList();
        for (BlockPos pos : masterBlocks) {
            craftersNBT.appendTag(new NBTTagLong(pos.toLong()));
        }

        nbt.setTag("crafters", craftersNBT);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList craftersNBT = nbt.getTagList("crafters", net.minecraftforge.common.util.Constants.NBT.TAG_LONG);
        for (NBTBase nbtLong : craftersNBT) {
            masterBlocks.add(BlockPos.fromLong(((NBTTagLong) nbtLong).getLong()));
        }
    }

}


