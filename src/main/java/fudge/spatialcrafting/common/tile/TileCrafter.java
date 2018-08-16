package fudge.spatialcrafting.common.tile;


import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.block.BlockCrafter.CrafterType;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

import static fudge.spatialcrafting.common.block.BlockCrafter.CrafterType.MASTER;


public class TileCrafter extends TileEntity {

    private BlockPos masterBlockPos;

    public TileCrafter() {
    }

    public int getCrafterSize() {
        return ((BlockCrafter) (world.getBlockState(pos).getBlock())).getCrafterSize();
    }

    public BlockPos getMasterPos() {
        return masterBlockPos;
    }

    public TileMasterCrafter getMasterCrafter() {

        if (masterBlockPos != null) {
            return Util.getTileEntity(world, getMasterPos());
        } else {
            SpatialCrafting.LOGGER.error(new NullPointerException("Attempt to get a master crafter while the masterBlockPos is null"));
            return null;
        }

    }

    public void scheduleCraft(World world, Block crafterBlock, int delay) {
        getMasterCrafter().scheduleCraft(world, crafterBlock, delay);
    }

    public List<BlockPos> getCrafterBlocks() {
        return getMasterCrafter().getCrafterBlocks();
    }


    public boolean isCrafting() {
        return getMasterCrafter().isCrafting();
    }

    public void bindToMasterBlock(BlockPos pos) {
        masterBlockPos = pos;
        this.markDirty();
    }

    public boolean craftTimePassed(World world) {
        return getMasterCrafter().craftTimePassed(world);
    }

    public void stopCrafting() {
        getMasterCrafter().stopCrafting();
    }

    public ItemStack[][][] getHologramInvArr() {
        return getMasterCrafter().getHologramInvArr();
    }

    public BlockPos[][][] getHolograms() {
        return getMasterCrafter().getHolograms();
    }


    protected NBTTagCompound serialized(NBTTagCompound existingData) {

        if (masterBlockPos != null) {
            existingData.setLong("masterBlock", masterBlockPos.toLong());
        } else {
            existingData.setLong("masterBlock", 0);
        }

        return existingData;

    }

    protected void deserialize(NBTTagCompound serializedData) {
        long masterLong = serializedData.getLong("masterBlock");
        if (masterLong != 0) {
            masterBlockPos = BlockPos.fromLong(masterLong);
        }
    }


    // Saves
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        return super.writeToNBT(this.serialized(existingData));
    }

    // Loads
    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        super.readFromNBT(serializedData);

        deserialize(serializedData);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            return true;
        }

        CrafterType oldType = oldState.getValue(BlockCrafter.TYPE);
        CrafterType newType = newState.getValue(BlockCrafter.TYPE);

        if (oldType.equals(MASTER) || newType.equals(MASTER)) {
            if (!oldType.equals(newType)) {
                return true;
            }
        }

        return false;

    }


    // Required for sending the saved info from the server instance to the client instance of the tile entity.
    @Override
    public void handleUpdateTag(NBTTagCompound data) {
        super.handleUpdateTag(data);
        deserialize(data);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.serialized(super.getUpdateTag());
    }


    public Vec3d centerOfHolograms() {
        BlockPos[][][] holograms = getHolograms();
        int size = holograms.length - 1;

        // Get the farthest away holograms from each other
        Vec3d edge1 = new Vec3d(holograms[0][0][0]);
        Vec3d edge2 = new Vec3d(holograms[size][size][size].add(1, 1, 1));

        return Util.middleOf(edge1, edge2);
    }

}

