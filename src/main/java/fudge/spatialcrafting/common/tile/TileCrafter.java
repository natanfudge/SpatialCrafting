package fudge.spatialcrafting.common.tile;


import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import fudge.spatialcrafting.client.sound.Sounds;
import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.*;
import fudge.spatialcrafting.common.util.MathUtil;
import fudge.spatialcrafting.common.util.RecipeUtil;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketStopParticles;
import kotlin.Unit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fudge.spatialcrafting.common.block.BlockHologram.ACTIVE;
import static fudge.spatialcrafting.common.util.MCConstants.NOTIFY_CLIENT;

public class TileCrafter extends TileEntity implements ITickable {

    private static final String OFFSET_NBT = "offset";
    private static final int ALL_ACTIVE = -1;
    private static final int SOUND_LOOP_TICKS = 27;
    private Offset offset;
    private int counter;

    public TileCrafter(BlockPos pos, BlockPos masterPos) {
        offset = new Offset(pos, masterPos);
    }


    public TileCrafter() {}

    public boolean isHelpActive() {
        return getRecipe() != null;
    }

    public void setActiveLayer(int layerToActivate) {
        setActiveLayer(layerToActivate, true);
    }

    public void setActiveLayer(int layerToActivate, boolean displayGhostItems) {
        getSharedData().setActiveLayer((byte)layerToActivate);

        getHolograms().indexedForEach((i, j, k, hologramPos) -> {

            IBlockState state = world.getBlockState(hologramPos);
            TileHologram hologram = Util.getTileEntity(world, hologramPos);

            // If i,j,k are within bounds
            if (shouldActivateHologram(layerToActivate, i, j, k)) {
                world.setBlockState(hologramPos, state.withProperty(ACTIVE, true), NOTIFY_CLIENT);

                // Display transparent item if applicable
                if (getRecipe() != null && displayGhostItems) {
                    ItemStack stack = RecipeUtil.getVisibleItemStack(getRecipe().getRequiredInput().get(i, j, k));
                    hologram.displayGhostItem(stack);
                } else {
                    hologram.stopDisplayingGhostItem();
                }

            } else if (state.getValue(ACTIVE)) {
                world.setBlockState(hologramPos, state.withProperty(ACTIVE, false), NOTIFY_CLIENT);
                hologram.stopDisplayingGhostItem();
            }

            // Weird Java-Kotlin interoperability bug?
            return Unit.INSTANCE;
        });
    }

    private boolean shouldActivateHologram(int layerToActivate, int i, int j, int k) {

        // This is for the purpose of crafting help
        if (getRecipe() != null) {
            int recipeSize = recipeSize();

            //If this is the correct layer and it is in bounds then check if this hologram is required for the recipe, otherwise false.
            if ((layerToActivate == ALL_ACTIVE || layerToActivate == i) && i < recipeSize && j < recipeSize && k < recipeSize) {
                // if the recipe is null there then it should not be activated.
                return getRecipe().getRequiredInput().get(i, j, k) != null || !isHelpActive();
            } else {
                return false;
            }


        } else {
            // This is for the up/down buttons
            int size = size();

            //If this is the correct layer and it is in bounds then return true, otherwise false.
            return (layerToActivate == ALL_ACTIVE || layerToActivate == i) && i < size && j < size && k < size;
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

    /*private boolean layerEnabled(int layer) {

        int size = size();
        CubeArr<BlockPos> holograms = getHolograms();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                try {
                    BlockPos hologramPos = holograms.get(layer, i, j);


                    if (world.getBlockState(hologramPos).getValue(ACTIVE)) return true;
                } catch (Exception e) {
                    int x = 2;
                }
            }

        }

        return false;
    }
*/
    private boolean activeLayerMatchesRecipe() {
        int layer = getActiveLayer();
        if(layer == ALL_ACTIVE) return true;

        SpatialRecipe recipe = getRecipe();

        if (recipe == null) return false;

        int size = recipeSize();
        CubeArr<BlockPos> holograms = getHolograms();
        CraftingInventory itemStacks = getHologramInvArr();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BlockPos hologramPos = holograms.get(layer, i, j);
                IItemStack stack = CraftTweakerMC.getIItemStack(itemStacks.get(layer, i, j));
                IIngredient requiredStack = recipe.getRequiredInput().get(layer, i, j);

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
        setActiveLayer(0);
        // In case a layer has already been done
        proceedHelp();
    }

    private int recipeSize() {
        SpatialRecipe recipe = getRecipe();
        if (recipe != null) {
            return recipe.getRequiredInput().getCubeSize();
        } else {
            return 0;
        }
    }


    public void proceedHelp() {
        int activeLayer = getActiveLayer();
        if (activeLayerMatchesRecipe()) {
            if (activeLayer != recipeSize() - 1) {
                setActiveLayer(activeLayer + 1);
                // Recursively proceeds help in case multiple layers already match the recipe
                proceedHelp();
            } else {
                // Last layer is treated differently
                activateAllLayers();
            }
        }
    }


    public void activateAllLayers() {
        setActiveLayer(ALL_ACTIVE, false);
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

    public int getActiveLayer() {
        return getSharedData().getActiveLayer();
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

    public CrafterPoses getCrafterBlocks() {
        return new CrafterPoses(size(), (i, j) -> masterPos().add(i, 0, j));
    }

    public CraftingInventory getHologramInvArr() {

        return new CraftingInventory(size(), (i, j, k) -> {

            CubeArr<BlockPos> holograms = getHolograms();

            // Due to the way Minecraft handles nulls in this case,
            // if there is an empty space in the blockPos array it will just put in air(which is what we want).
            TileHologram hologramTile = Util.getTileEntity(world, holograms.get(i, j, k));

            return hologramTile.getStoredItem();

        });

    }

    /**
     * Returns the holograms bound to this tileCrafter.
     * array[i][j][k] is defined as the hologram which has a offset of y = i+1, x = j, z = k from the masterPos, or: array[y-1][x][z]
     */
    public CubeArr<BlockPos> getHolograms() {

        int size = size();
        CrafterPoses crafters = getCrafterBlocks();


        return new CubeArr<>(size, (i, j, k) -> crafters.get(j, k).add(0, i + 1, 0));

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
        CubeArr<BlockPos> holograms = getHolograms();

        // Get the farthest away holograms from each other
        Vec3d edge1 = new Vec3d(holograms.firstElement());
        Vec3d edge2 = new Vec3d(holograms.lastElement().add(1, 1, 1));

        return MathUtil.middleOf(edge1, edge2);
    }

    @Override
    public void update() {
        if (!isMaster()) return;

        if (!world.isRemote && isCrafting()) {
            if (counter == SOUND_LOOP_TICKS) {
                counter = 0;
                world.playSound(null, pos, Sounds.CRAFT_LOOP, SoundCategory.BLOCKS, 0.8f, 0.8f);
            } else {
                counter++;
            }

        }

        // Update gets called once before the shared data is synced to the client, meaning it will be null at that time.
        // This is a fix to the errors it causes.
        if (WorldSavedDataCrafters.getDataForMasterPos(world, masterPos()) == null) return;

        if (craftTimeHasPassed()) {
            stopHelp();

            if (!world.isRemote) {
                // server
                completeCrafting(world);
            } else {
                // client
                this.resetCraftingState();
            }


        }

    }

    private void completeCrafting(World world) {

        this.resetCraftingState();

        // Calculates the point at which the particle will end to decide where to drop the item.
        Vec3d center = centerOfCrafters().add(0, 1.8, 0);

        // Find the correct recipe to craft with
        for (SpatialRecipe recipe : SpatialRecipe.getRecipes()) {
            if (recipe.matches(getHologramInvArr()) && !this.isCrafting()) {
                // Finally, drop the item on the ground.
                Util.dropItemStack(world, center, recipe.getOutput(), false);
            }
        }

        // Removes the existing items
        getHolograms().forEach(blockPos -> Util.<TileHologram>getTileEntity(world, blockPos).removeItem(1, true));


        // Play end sound
        world.playSound(null, pos, Sounds.CRAFT_END, SoundCategory.BLOCKS, 0.2f, 0.8f);


    }

    public Vec3d centerOfCrafters() {
        CrafterPoses crafters = getCrafterBlocks();

        return MathUtil.middleOf(new Vec3d(crafters.firstCrafter()), (new Vec3d(crafters.lastCrafter())).add(1, 0, 1));

    }


    public void stopHelp() {

        // If the recipe is removed before, then all holograms will activate
        setRecipe(null);
        activateAllLayers();

    }


}

