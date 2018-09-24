package fudge.spatialcrafting.common.tile;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.tile.util.Offset;
import fudge.spatialcrafting.common.util.MCConstants;
import fudge.spatialcrafting.common.util.RecipeUtil;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileHologram extends TileEntity {

    private static final String INVENTORY_NBT = "inventory";
    private static final String LAST_CHANGE_TIME_NBT = "lastChangeTime";
    private static final String MASTER_BLOCK_NBT = "masterBlock";
    private static final String DISPLAYING_GHOST_ITEM_NBT = "displayingGhostItem";
    private long lastChangeTime;
    private boolean displayingGhostItem = false;
    private ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            Util.<TileHologram>getTileEntity(world, pos).setLastChangeTime(world.getTotalWorldTime());
            markDirty();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private BlockPos masterBlockPos;

    public boolean isDisplayingGhostItem() {
        return displayingGhostItem;
    }

    public void displayGhostItem(ItemStack item) {
        if (isEmpty()) {
            if(isDisplayingGhostItem()) stopDisplayingGhostItem();
            getItemHandler().insertItem(0, item, false);
            displayingGhostItem = true;
        }
    }

    public void stopDisplayingGhostItem() {
        if (isDisplayingGhostItem()) {
            getItemHandler().extractItem(0, 1, false);
            displayingGhostItem = false;
        }
    }

    public BlockPos getMasterPos() {
        return masterBlockPos;
    }


    public TileCrafter getCrafter() {
        return Util.getTileEntity(world, getMasterPos());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
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


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(1, 2, 1));
    }

    protected NBTTagCompound serialized(NBTTagCompound existingData) {

        existingData.setTag(INVENTORY_NBT, inventory.serializeNBT());
        existingData.setLong(LAST_CHANGE_TIME_NBT, lastChangeTime);
        existingData.setBoolean(DISPLAYING_GHOST_ITEM_NBT, displayingGhostItem);
        try {
            existingData.setLong(MASTER_BLOCK_NBT, masterBlockPos.toLong());
        } catch (NullPointerException e) {
            SpatialCrafting.LOGGER.log(Level.ERROR,
                    "[Spatial Crafting] A hologram exists without being bound to a multiblock at " + pos + " ! Report to the mod author if this happens normally");
        }

        return existingData;
    }

    protected void deserialize(NBTTagCompound serializedData) {
        inventory.deserializeNBT(serializedData.getCompoundTag(INVENTORY_NBT));
        lastChangeTime = serializedData.getLong(LAST_CHANGE_TIME_NBT);
        displayingGhostItem = serializedData.getBoolean(DISPLAYING_GHOST_ITEM_NBT);
        try {
            masterBlockPos = BlockPos.fromLong(serializedData.getLong(MASTER_BLOCK_NBT));
        } catch (NullPointerException e) {
            SpatialCrafting.LOGGER.log(Level.ERROR,
                    "[Spatial Crafting] A hologram exists without being bound to a multiblock at " + pos + " ! Report to the mod author if this happens normally");
        }
    }


    // Saves inventory
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        return super.writeToNBT(this.serialized(existingData));
    }

    // Loads inventory
    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        super.readFromNBT(serializedData);
        deserialize(serializedData);
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

    private IItemHandler getItemHandler() {
        IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        assert itemHandler != null;
        return itemHandler;
    }

    public ItemStack insertItem(ItemStack item) {
        stopDisplayingGhostItem();
        return getItemHandler().insertItem(0, item, false);
    }

    public ItemStack extractItem(int amount) {
        ItemStack extractedItem = isDisplayingGhostItem() ? ItemStack.EMPTY : getItemHandler().extractItem(0, amount, false);
        // Replace the extracted item with a ghost item if applicable
        displayGhostItem(getPreviouslyStoredGhostItem());
        return extractedItem;
    }

    private Offset getOffset() {
        return new Offset(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), getCrafter().masterPos());
    }

    private ItemStack getPreviouslyStoredGhostItem() {
        TileCrafter crafter = getCrafter();

        if (crafter.getRecipe() != null) {
            return RecipeUtil.getVisibleItemStack(crafter.getRecipe().getRequiredInput().get(getOffset()));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack getStoredItem() {
        return isDisplayingGhostItem() ? ItemStack.EMPTY : getItemHandler().getStackInSlot(0);
    }


    @Override
    public void handleUpdateTag(NBTTagCompound data) {
        super.handleUpdateTag(data);
        deserialize(data);
    }

    public boolean isEmpty() {
        return isDisplayingGhostItem() || getItemHandler().extractItem(0, 1, true).equals(ItemStack.EMPTY);
    }


    public void removeItem(int amount, boolean informClient) {
        this.getItemHandler().extractItem(0, amount, false);
        if (informClient) {
            IBlockState state = this.getBlockState();
            world.notifyBlockUpdate(new BlockPos(pos), state, state, MCConstants.NOTIFY_CLIENT);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, this.serialized(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        deserialize(pkt.getNbtCompound());
    }

    private IBlockState getBlockState() {
        return world.getBlockState(pos);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.serialized(super.getUpdateTag());
    }


}

