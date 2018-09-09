package fudge.spatialcrafting.common.tile;


import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.Offset;
import fudge.spatialcrafting.common.util.ArrayUtil;
import fudge.spatialcrafting.common.util.MathUtil;
import fudge.spatialcrafting.common.util.RecipeUtil;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketStopParticles;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fudge.spatialcrafting.common.MCConstants.NOTIFY_CLIENT;
import static fudge.spatialcrafting.common.block.BlockCrafter.CRAFT_DURATION_MULTIPLIER;
import static fudge.spatialcrafting.common.block.BlockHologram.ACTIVE;

public class TileCrafter extends TileEntity implements ITickable {

    private static final String OFFSET_NBT = "offset";
    private static final int ACTIVATE_ALL = -1;
    private Offset offset;

    public TileCrafter(BlockPos pos, BlockPos masterPos) {
        offset = new Offset(pos, masterPos);
    }

    public TileCrafter() {}

    public boolean isHelpActive() {
        return getRecipe() != null;
    }


    public void setActiveHolograms(int layerToActivate) {
        setActiveHolograms(layerToActivate, true);
    }

    public void setActiveHolograms(int layerToActivate, boolean displayGhostItems) {
        int crafterSize = size();

        for (int i = 0; i < crafterSize; i++) {
            for (int j = 0; j < crafterSize; j++) {
                for (int k = 0; k < crafterSize; k++) {
                    BlockPos hologramPos = getHolograms()[i][j][k];
                    IBlockState state = world.getBlockState(hologramPos);
                    TileHologram hologram = Util.getTileEntity(world, hologramPos);

                    // If i,j,k are within bounds
                    if (shouldActivateHologram(layerToActivate, i, j, k)) {
                        world.setBlockState(hologramPos, state.withProperty(ACTIVE, true), NOTIFY_CLIENT);

                        // Display transparent item if applicable
                        if (getRecipe() != null && displayGhostItems) {
                            ItemStack stack = RecipeUtil.getVisibleItemStack(getRecipe().getRequiredInput()[i][j][k]);
                            hologram.displayGhostItem(stack);
                        } else {
                            hologram.stopDisplayingGhostItem();
                        }

                    } else if (state.getValue(ACTIVE)) {
                        world.setBlockState(hologramPos, state.withProperty(ACTIVE, false), NOTIFY_CLIENT);
                        hologram.stopDisplayingGhostItem();
                    }
                }
            }

        }
    }


    private boolean shouldActivateHologram(int layerToActivate, int i, int j, int k) {

        // This is for the purpose of crafting help
        if (getRecipe() != null) {
            int recipeSize = recipeSize();

            //If this is the correct layer and it is in bounds then check if this hologram is required for the recipe, otherwise false.
            if ((layerToActivate == ACTIVATE_ALL || layerToActivate == i) && i < recipeSize && j < recipeSize && k < recipeSize) {
                // if the recipe is null there then it should not be activated.
                return getRecipe().getRequiredInput()[i][j][k] != null || !isHelpActive();
            } else {
                return false;
            }


        } else {
            // This is for the up/down buttons
            int size = size();

            //If this is the correct layer and it is in bounds then return true, otherwise false.
            return (layerToActivate == ACTIVATE_ALL || layerToActivate == i) && i < size && j < size && k < size;
        }

    }


    /**
     */
    @Nullable
    public SpatialRecipe getRecipe() {
        return getSharedData().getRecipe();
    }

    public void setRecipe(@Nullable SpatialRecipe recipe) {
        getSharedData().setRecipe(recipe);
    }

    private boolean layerEnabled(int layer) {

        int size = size();
        BlockPos[][] holograms = getHolograms()[layer];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BlockPos hologramPos = holograms[i][j];

                if (world.getBlockState(hologramPos).getValue(ACTIVE)) return true;
            }

        }

        return false;
    }

    private boolean layerMatchesRecipe(int layer) {
        SpatialRecipe recipe = getRecipe();

        if (recipe == null) return false;

        int size = recipeSize();
        BlockPos[][] holograms = getHolograms()[layer];
        ItemStack[][] itemStacks = getHologramInvArr()[layer];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BlockPos hologramPos = holograms[i][j];
                IItemStack stack = CraftTweakerMC.getIItemStack(itemStacks[i][j]);
                IIngredient requiredStack = recipe.getRequiredInput()[layer][i][j];

                // If the hologram is not active it counts as complete
                if (world.getBlockState(hologramPos).getValue(ACTIVE) && !RecipeUtil.nullSafeMatch(requiredStack, stack)) {
                    return false;
                }

            }
        }

        return true;

    }

    public void startHelp(SpatialRecipe recipe) {

        setRecipe(recipe);
        setActiveHolograms(0);
        // In case a layer has already been done
        proceedHelp();
    }

    private int recipeSize() {
        SpatialRecipe recipe = getRecipe();
        if (recipe != null) {
            return recipe.getRequiredInput().length;
        } else {
            return 0;
        }
    }

    public void proceedHelp() {
        for (int i = 0; i < recipeSize(); i++) {
            if (layerEnabled(i) && layerMatchesRecipe(i)) {
                if (i != recipeSize() - 1) {
                    setActiveHolograms(i + 1);

                    // Recursively activates layers in case multiple layers already match the recipe
                    proceedHelp();
                    return;
                } else {
                    // Last layer is treated differently
                    activateAllLayers();

                }
            }
        }
    }


    public void activateAllLayers() {
        setActiveHolograms(ACTIVATE_ALL, false);
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

    private CraftersData getSharedData() {
        CraftersData data = (CraftersData) WorldSavedDataCrafters.getDataForMasterPos(world, masterPos());
        if (data != null) {
            return data;
        } else {
            throw new NullPointerException(String.format("Cannot find data for masterPos %s at pos %s in %s world",
                    masterPos(),
                    pos,
                    world.isRemote ? "CLIENT" : "SERVER"));
        }
    }

    public long getCraftEndTime() {
        return getSharedData().getCraftTime();
    }


    public void setCraftEndTime(long time) {
        getSharedData().setCraftTime(time);
    }

    public void resetCraftingState() {
        resetCraftingState(false);
    }

    public void resetCraftingState(boolean sendDisableParticlesPacket) {
        setCraftEndTime(0);

        if (sendDisableParticlesPacket) {
            final int RANGE = 64;
            PacketHandler.getNetwork().sendToAllAround(new PacketStopParticles(masterPos()),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), RANGE));
        }
    }

    private boolean craftTimeHasPassed() {
        return getCraftEndTime() != 0 && world.getWorldTime() >= getCraftEndTime();
    }

    public boolean isCrafting() {
        return getCraftEndTime() != 0;
    }

    public void scheduleCraft(World world, int delay) {
        setCraftEndTime(world.getWorldTime() + delay);
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

    public ItemStack[][][] getHologramInvArr() {

        int size = size();
        ItemStack[][][] returning = new ItemStack[size][size][size];

        BlockPos[][][] holograms = getHolograms();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    // Due to the way Minecraft handles nulls in this case,
                    // if there is an empty space in the blockPos array it will just put in air(which is what we want).
                    TileHologram hologramTile = Util.getTileEntity(world, holograms[i][j][k]);

                    returning[i][j][k] = hologramTile.getStoredItem();
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

        int size = size();
        BlockPos[][][] holograms = new BlockPos[size][size][size];


        BlockPos[][] crafters = getCrafterBlocks();

        ArrayUtil.innerForEach2D(crafters, crafterPos -> {
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

        return MathUtil.middleOf(edge1, edge2);
    }


    @Override
    public void update() {
        if (!isMaster()) return;

        // Update gets called once before the shared data is synced to the client, meaning it will be null at that time.
        // This is a fix to the errors it causes.
        if (WorldSavedDataCrafters.getDataForMasterPos(world, masterPos()) == null) return;

        if (craftTimeHasPassed()) {
            stopHelp();

            if (!world.isRemote) {
                completeCrafting(world);
            } else {
                this.resetCraftingState();
            }


        }

    }

    private void completeCrafting(World world) {

        this.resetCraftingState();

        // Calculates the point at which the particle will end to decide where to drop the item.
        Vec3d center = centerOfHolograms();
        int durationTicks = this.size() * CRAFT_DURATION_MULTIPLIER * MCConstants.TICKS_PER_SECOND;
        //TODO change this accordingly to the slamdown
        double newY = center.y;
        Vec3d endPos = new Vec3d(center.x, newY, center.z);

        // Find the correct recipe to craft with
        for (SpatialRecipe recipe : SpatialRecipe.getRecipes()) {
            if (recipe.matches(getHologramInvArr()) && !this.isCrafting()) {
                // Finally, drop the item on the ground.
                Util.dropItemStack(world, endPos, recipe.getOutput());
            }
        }

        // Removes the existing items
        ArrayUtil.innerForEach(getHolograms(), blockPos -> Util.<TileHologram>getTileEntity(world, blockPos).removeItem(1, true));


    }


    public void stopHelp() {

        // If the recipe is removed before, then all holograms will activate
        setRecipe(null);
        activateAllLayers();

    }


}

