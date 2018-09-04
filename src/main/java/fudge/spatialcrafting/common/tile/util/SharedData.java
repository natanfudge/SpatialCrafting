package fudge.spatialcrafting.common.tile.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Currently hardcoded for CraftersData
 */
public abstract class SharedData {

    private static final String MASTER_POS_NBT = "masterPos";
    private BlockPos masterPos;

    public SharedData(BlockPos pos) {
        masterPos = pos;
    }

    public SharedData(NBTTagCompound serializedData) {
        deserialize(serializedData);
    }


    public NBTTagCompound serialized(NBTTagCompound existingData) {
        existingData.setLong(MASTER_POS_NBT, masterPos.toLong());
        return this.writeToNBT(existingData);
    }

    private void deserialize(NBTTagCompound serializedData) {
        masterPos = BlockPos.fromLong(serializedData.getLong(MASTER_POS_NBT));
        this.readFromNBT(serializedData);

    }


    abstract NBTTagCompound writeToNBT(NBTTagCompound existingData);

    abstract void readFromNBT(NBTTagCompound serializedData);


    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos pos) {
        masterPos = pos;
    }


}
