package fudge.spatialcrafting.common.data;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Currently hardcoded for CraftersData
 */
@Mod.EventBusSubscriber
public abstract class SharedData {


        private static final String MASTER_POS_NBT = "masterPos";
    private BlockPos masterPos;
    protected boolean dirty;

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void markDirty(){
        setDirty(true);
    }

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
        markDirty();
    }


}
