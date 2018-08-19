/*
package fudge.spatialcrafting.common.tile;

import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileMasterCrafter extends TileCrafter {

    private static final String HOLOGRAMS_NBT = "hologram_list";
    private static final String CRAFTERS_NBT = "crafter_list";
    private static final String PENDING_CRAFT_NBT = "pending_craft_time";


    private BlockPos[][][] hologramPosArr;
    private List<BlockPos> crafterBlockList = new LinkedList<>();
    private long craftEndTime = 0;

    public TileMasterCrafter(BlockPos[][][] holograms, List<BlockPos> crafters) {
        super();

        this.hologramPosArr = holograms;
        this.crafterBlockList = crafters;
    }

    // Required so newInstance() won't break
    public TileMasterCrafter() {
        super();
    }

    */
/**
     * All transformations from list to array and back are EXTREMELY SENSITIVE! Everything must be put back exactly where it was.
     *//*

    private static BlockPos[][][] listToArray(NBTTagList list) {

        double sizeD = Math.cbrt(list.tagCount());
        // Make certain that the list is exactly (crafter size)^3   (whole number).
        assert sizeD == Math.floor(sizeD);
        int size = (int) sizeD;

        BlockPos[][][] returning = new BlockPos[size][size][size];

        int i = 0, j = 0, k = 0;
        for (NBTBase hologramPos : list) {

            returning[i][j][k] = BlockPos.fromLong(((NBTTagLong) hologramPos).getLong());

            k++;
            if (k > size - 1) {
                k = 0;
                j++;
            }
            if (j > size - 1) {
                j = 0;
                i++;
            }
        }

        return returning;


    }

    @Nullable
    public static TileMasterCrafter getClosestMasterBlock(World world, BlockPos pos) {
        List<BlockPos> poses = WorldSavedDataCrafters.getMasterBlocks(world);

        // There are no master blocks
        if (poses.size() == 0) {
            return null;
        }

        // Find closest block
        BlockPos closestPos = poses.get(0);
        for (BlockPos currentPos : poses) {
            if (Util.minimalDistanceOf(pos, currentPos) < Util.minimalDistanceOf(pos, closestPos)) {
                closestPos = currentPos;
            }
        }

        return (TileMasterCrafter) world.getTileEntity(closestPos);
    }

    @Override
    public boolean isCrafting() {
        return craftEndTime != 0;
    }

    @Override
    public void scheduleCraft(World world, Block crafterBlock, int delay) {
        world.scheduleUpdate(new BlockPos(this.pos), crafterBlock, delay);
        craftEndTime = world.getWorldTime() + delay;
        markDirty();
    }

    @Override
    public void stopCrafting() {
        craftEndTime = 0;
        markDirty();
    }

    @Override
    public boolean craftTimeHasPassed(World world) {
        return world.getWorldTime() >= craftEndTime;
    }

    @Override
    public BlockPos getMasterPos() {
        return this.pos;
    }

    @Override
    public BlockPos[][][] getHolograms() {
        return hologramPosArr;
    }

    @Override
    public List<BlockPos> getCrafterBlocks() {
        return crafterBlockList;
    }

    @Override
    public ItemStack[][][] getHologramInvArr() {
        int size = ((BlockCrafter) getBlockType()).size();

        ItemStack[][][] returning = new ItemStack[size][size][size];


        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    // Due to the way Minecraft handles nulls in this case,
                    // if there is an empty space in the blockPos array it will just put in air(which is what we want).
                    TileEntity hologramTile = world.getTileEntity(getHolograms()[i][j][k]);
                    IItemHandler itemHandler = hologramTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                    returning[i][j][k] = itemHandler.getStackInSlot(0);
                }
            }
        }

        return returning;
    }


    */
/**
     * Converts the info in the TileMasterCrafter to an NBTTagCompound and adds it to the existing data.
     *//*

    @Override
    protected NBTTagCompound serialized(NBTTagCompound existingData) {

        // Serialize holograms
        NBTTagList hologramsNBT = new NBTTagList();
        Util.innerForEach(hologramPosArr, (hologramPos) -> hologramsNBT.appendTag(new NBTTagLong(hologramPos.toLong())));
        existingData.setTag(HOLOGRAMS_NBT, hologramsNBT);

        // Serialize crafters
        NBTTagList craftersNBT = new NBTTagList();
        crafterBlockList.forEach(crafterPos -> craftersNBT.appendTag(new NBTTagLong(crafterPos.toLong())));
        existingData.setTag(CRAFTERS_NBT, craftersNBT);

        //Serialize pending craft
        existingData.setLong(PENDING_CRAFT_NBT, craftEndTime);


        return super.serialized(existingData);
    }


    */
/**
     * Takes the serialized info from a NBTTagCompound and assigns the values to the TileMasterCrafter in normal data form.
     *//*

    @Override
    protected void deserialize(NBTTagCompound serializedData) {

        super.deserialize(serializedData);

        // Deserialize holograms
        NBTTagList hologramNBT = serializedData.getTagList(HOLOGRAMS_NBT, Constants.NBT.TAG_LONG);
        hologramPosArr = listToArray(hologramNBT);

        // Deserialize crafters
        NBTTagList crafterNBT = serializedData.getTagList(CRAFTERS_NBT, Constants.NBT.TAG_LONG);
        crafterNBT.forEach(crafterPos -> crafterBlockList.add(BlockPos.fromLong(((NBTTagLong) crafterPos).getLong())));

        // Deserialize pending craft
        craftEndTime = serializedData.getLong(PENDING_CRAFT_NBT);

    }

   */
/* @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound existingData) {
        return super.writeToNBT(this.serialized(existingData));
    }

    // Loads
    @Override
    public void readFromNBT(NBTTagCompound serializedData) {
        super.readFromNBT(serializedData);

        deserialize(serializedData);

    }*//*


    @Override
    public void handleUpdateTag(NBTTagCompound data) {
        super.handleUpdateTag(data);
        deserialize(data);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.serialized(super.getUpdateTag());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return super.shouldRefresh(world, pos, oldState, newState);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound data = new NBTTagCompound();
        data.setLong(PENDING_CRAFT_NBT, craftEndTime);
        return new SPacketUpdateTileEntity(getPos(), 1, data);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        NBTTagCompound serializedData = packet.getNbtCompound();
        craftEndTime = serializedData.getLong(PENDING_CRAFT_NBT);
    }
}

*/
