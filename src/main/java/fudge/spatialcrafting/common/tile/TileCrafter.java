package fudge.spatialcrafting.common.tile;


import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.block.SCBlocks;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static fudge.spatialcrafting.common.SCConstants.NOTIFY_CLIENT;
import static fudge.spatialcrafting.common.block.BlockCrafter.CrafterType.FORMED;
import static fudge.spatialcrafting.common.block.BlockCrafter.TYPE;


public class TileCrafter extends TileEntity {

    private Offset offset;

    private static final String OFFSET_NBT = "offset";

    public TileCrafter() {
    }

    public TileCrafter(BlockPos pos, BlockPos masterPos) {
        offset = new Offset(pos, masterPos);
    }

    public int size() {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof BlockCrafter) {
            return ((BlockCrafter) block).size();
        } else {


            // The master serves as a backup block in case this one's gone.
            if (!isMaster()) {
                block = world.getBlockState(masterPos()).getBlock();

                // If this is already the master then get some other one as replacement
            } else {
                block = world.getBlockState(pos.add(1, 0, 0)).getBlock();
            }

            if (block instanceof BlockCrafter) {
                return ((BlockCrafter) block).size();
            } else {
                throw new NullPointerException("Crafter blocks do not exist and therefore size cannot be returned.");
            }
        }

    }


    public boolean isMaster() {
        return offset.equals(Offset.NONE);
    }

    public TileCrafter master() {

        if (this.isMaster()) return this;

        return Util.getTileEntity(world, this.masterPos());

    }

    public BlockPos masterPos() {

        if (offset.equals(Offset.NONE)) return pos;

        return offset.adjustToMaster(this.pos);
    }

    // In future might be better to return a "SharedInfo" object
    public long getSharedData() {
        return WorldSavedDataCrafters.getDataForMasterPos(world, masterPos());
    }

    // In future might be better to return a "SharedInfo" object
    public void setSharedData(long craftEndTime) {
        WorldSavedDataCrafters.setDataForMasterPos(world, masterPos(), craftEndTime);
    }

    public void stopCrafting() {
        setSharedData(0);
    }

    public boolean craftTimeHasPassed() {
        return world.getWorldTime() >= getSharedData();
    }

    public boolean isCrafting() {
        return getSharedData() != 0;
    }


    public void scheduleCraft(World world, Block crafterBlock, int delay) {
        world.scheduleUpdate(new BlockPos(masterPos()), crafterBlock, delay);
        setSharedData(world.getWorldTime() + delay);
    }


    public BlockPos[][] getCrafterBlocks() {
        int size = size();

        BlockPos[][] positions = new BlockPos[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                positions[i][j] = masterPos().add(i, 0, j);
            }
        }

        return positions;

    }


    public void bindToMasterBlock(BlockPos slavePos, BlockPos masterPos) {
        offset = new Offset(slavePos, masterPos);
        this.markDirty();
    }


    @Nullable
    public ItemStack[][][] getHologramInvArr() {


        int size = size();
        ItemStack[][][] returning = new ItemStack[size][size][size];

        BlockPos[][][] holograms = getHolograms();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    // Due to the way Minecraft handles nulls in this case,
                    // if there is an empty space in the blockPos array it will just put in air(which is what we want).
                    TileEntity hologramTile = world.getTileEntity(holograms[i][j][k]);
                    IItemHandler itemHandler = hologramTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                    returning[i][j][k] = itemHandler.getStackInSlot(0);
                }
            }
        }

        return returning;
    }

    /**
     * Returns the holograms bound to this tileCrafter.
     * array[i][j][k] is defined as the hologram which has a offset of y = i+1, x = j, z = k from the masterPos, or: array[y-1][x][z]
     */
    public BlockPos[][][] getHolograms() {
        //TODO: make certain this is done correctly

        int size = size();
        BlockPos[][][] holograms = new BlockPos[size][size][size];


        //TODO: might not be completely accurate, need to use helper methods to adjust

        BlockPos[][] crafters = getCrafterBlocks();

        Util.innerForEach2D(crafters, crafterPos -> {
            for (int i = 0; i < size; i++) {
                Offset crafterOffset = new Offset(crafterPos, masterPos());

                // May need to swap this
                holograms[i][crafterOffset.getX()][crafterOffset.getZ()] = crafterPos.add(0, i + 1, 0);
            }
        });

        return holograms;


    }



    protected NBTTagCompound serialized(NBTTagCompound existingData) {

        existingData.setLong(OFFSET_NBT, offset.toLong());

        return existingData;

    }

    protected void deserialize(NBTTagCompound serializedData) {

        offset = Offset.fromLong(serializedData.getLong(OFFSET_NBT));
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
        return oldState.getBlock() != newState.getBlock();
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

    public static BlockPos createMultiblock(World world, List<BlockPos> crafterList, int crafterSize) {
        BlockPos masterPos = getMasterPosFromList(crafterList);


        BlockPos[][][] holograms = new BlockPos[crafterSize][crafterSize][crafterSize];

        int j = 0, k = 0;

        //TODO: might not be completely accurate, need to use helper methods to adjust

        for (BlockPos crafterPos : crafterList) {

            // Connect crafters to the multiblock
            // Change the crafters' blockstate and tile entity
            world.setBlockState(crafterPos, world.getBlockState(crafterPos).withProperty(TYPE, FORMED), NOTIFY_CLIENT);
            TileCrafter tileCrafter = new TileCrafter(crafterPos, masterPos);
            world.setTileEntity(crafterPos, tileCrafter);


            for (int i = 1; i < crafterSize + 1; i++) {
                // Place hologram
                BlockPos hologramPos = crafterPos.add(0, i, 0);
                world.setBlockState(hologramPos, SCBlocks.HOLOGRAM.getDefaultState());
                holograms[i - 1][j][k] = hologramPos;

                // Connect holograms to the multiblock
                TileHologram hologramTile = Util.getTileEntity(world, hologramPos);
                hologramTile.bindToMasterBlock(masterPos);
            }


            k++;
            if (k >= crafterSize) {
                j++;
                k = 0;
            }
        }


        return masterPos;

    }

    // MasterPos will be the one with the lowest x and z coordinates.
    // This way all other poses offset will start from (0,0,0) (masterPos offset) and increase as they are farther away from masterPos.
    private static BlockPos getMasterPosFromList(List<BlockPos> crafterList) {
        BlockPos masterPos = crafterList.get(0);
        for (BlockPos crafterPos : crafterList) {
            if (crafterPos.getZ() + crafterPos.getX() < masterPos.getX() + masterPos.getZ()) {
                masterPos = crafterPos;
            }
        }

        return masterPos;
    }

    @Nullable
    public static TileCrafter getClosestMasterBlock(World world, BlockPos pos) {
        Set<BlockPos> poses = WorldSavedDataCrafters.getMasterBlocks(world);

        // There are no master blocks
        if (poses.isEmpty()) {
            return null;
        }

        // Find closest block
        BlockPos closestPos = (BlockPos) poses.toArray()[0];
        for (BlockPos currentPos : poses) {
            if (Util.minimalDistanceOf(pos, currentPos) < Util.minimalDistanceOf(pos, closestPos)) {
                closestPos = currentPos;
            }
        }

        return Util.getTileEntity(world, closestPos);
    }


}

