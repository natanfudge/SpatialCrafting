package fudge.spatialcrafting.common.tile;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.SCConstants;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.block.PacketUpdateHologram;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileHologram extends TileEntity {
    private long lastChangeTime;
    private ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!world.isRemote) {
                lastChangeTime = world.getTotalWorldTime();
                // Sends a packet from the server to the clients to update them that a change has occured
                PacketHandler.getNetwork().sendToAllAround(new PacketUpdateHologram(TileHologram.this),
                        new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                                pos.getX(),
                                pos.getY(),
                                pos.getZ(),
                                SCConstants.NORMAL_ITEMSTACK_LIMIT));
                markDirty();
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private BlockPos masterBlockPos;

    public BlockPos getMasterPos() {
        return masterBlockPos;
    }


    public TileCrafter getMasterCrafter() {
        return Util.getTileEntity(world, getMasterPos());
    }


    public int getCrafterSize() {
        return ((BlockCrafter) world.getBlockState(this.getMasterPos()).getBlock()).size();
    }

    public void bindToMasterBlock(BlockPos pos) {
        masterBlockPos = pos;
    }

    public long getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(long time) {
        this.lastChangeTime = time;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }


    @Nullable
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }

    // Saves inventory
    @Nullable
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setLong("lastChangeTime", lastChangeTime);
        try {
            compound.setLong("masterBlock", masterBlockPos.toLong());
        } catch (NullPointerException e) {
            SpatialCrafting.LOGGER.log(Level.ERROR,
                    "[Spatial Crafting] A hologram exists without being bound to a multiblock at " + pos + " ! Report to the mod author if this happens normally");
        }

        return super.writeToNBT(compound);
    }

    // Loads inventory
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        lastChangeTime = compound.getLong("lastChangeTime");
        try {
            masterBlockPos = BlockPos.fromLong(compound.getLong("masterBlock"));
        } catch (NullPointerException e) {
            SpatialCrafting.LOGGER.log(Level.ERROR,
                    "[Spatial Crafting] A hologram exists without being bound to a multiblock at " + pos + " ! Report to the mod author if this happens normally");
        }

        super.readFromNBT(compound);
    }


    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    //todo: changed, might cause problems
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        Minecraft.getMinecraft().world.notifyBlockUpdate(pos, BlockCrafter.DEFAULT_STATE, BlockCrafter.DEFAULT_STATE, 1);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Nullable
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }


}

