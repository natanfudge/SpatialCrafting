package fudge.spatialcrafting.common.block;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.client.particle.ParticleItemDust;
import fudge.spatialcrafting.common.SCConstants;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.*;

import static fudge.spatialcrafting.common.SCConstants.BLOCK_UPDATE;
import static fudge.spatialcrafting.common.SCConstants.NOTIFY_CLIENT;
import static fudge.spatialcrafting.common.block.BlockCrafter.CrafterType.*;
import static fudge.spatialcrafting.common.tile.TileCrafter.createMultiblock;


@SuppressWarnings("deprecation")
public class BlockCrafter extends BlockTileEntity<TileCrafter> {


    public static final int CRAFT_DURATION_MULTIPLIER = 5;
    /**
     * Can be "UNFORMED, FORMED, and MASTER
     */
    public static final PropertyEnum<CrafterType> TYPE = PropertyEnum.create("type", CrafterType.class);
    public static final IBlockState DEFAULT_STATE = new BlockCrafter(1).blockState.getBaseState().withProperty(TYPE, UNFORMED);
    private final int crafterSize;


    public BlockCrafter(int size) {
        super(Material.WOOD);

        this.crafterSize = size;
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, UNFORMED));
        this.setHardness(2.0F);
        this.setSoundType(SoundType.WOOD);

    }

    public int size() {
        return crafterSize;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == 0) return this.getDefaultState().withProperty(TYPE, UNFORMED);
        if (meta == 1) return this.getDefaultState().withProperty(TYPE, FORMED);
        if (meta == 2) return this.getDefaultState().withProperty(TYPE, MASTER);

        // Should never get here
        return this.getDefaultState().withProperty(TYPE, UNFORMED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(TYPE).equals(UNFORMED)) return 0;
        if (state.getValue(TYPE).equals(FORMED)) return 1;
        if (state.getValue(TYPE).equals(MASTER)) return 2;

        // Should never get here
        return 0;
    }

    /**
     * Called when the block is placed by any source.
     */
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        // onBlockPlacedBy(worldIn, pos, state, null, null);
        //TODO: Might not work when placed by a machine
    }

    /**
     * Called when a block is placed by something.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        // The master block is the last block placed before the multiblock was formed
        List<BlockPos> crafterBlocks = getPossibleMultiblock(world, pos);
        if (crafterBlocks != null) {
            // Searches for space above it
            if (spaceExists(world, crafterBlocks, size())) {

                BlockPos masterPos = createMultiblock(world, crafterBlocks, crafterSize);
                // Stores the masterblock for the purposes of iterating through available crafters.
                WorldSavedDataCrafters.addMasterBlock(world, masterPos);


            } else if (world.isRemote && placer instanceof EntityPlayer) {
                ((EntityPlayer) (placer)).sendStatusMessage(new TextComponentTranslation("tile.spatialcrafting.blockcrafter.no_space", 0), true);

            }
        }

    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        try {
            if (!state.getValue(TYPE).equals(UNFORMED) && playerIn.getHeldItem(hand).isEmpty()) {
                TileCrafter crafter = Util.getTileEntity(world, pos);
                assert crafter != null;

                ItemStack[][][] craftingInventory = crafter.getHologramInvArr();


                if (!crafter.isCrafting()) {
                    // Check if any recipe matches, if so, beginCraft the recipe.
                    for (SpatialRecipe recipe : SpatialRecipe.getRecipes()) {
                        if (recipe.matches(craftingInventory)) {
                            beginCraft(world, pos);
                        }
                    }
                }

                return true;

            }


        } catch (Exception e) {
            SpatialCrafting.LOGGER.error("Error in BlockCrafter::OnBlockActivated", e);
        }

        return false;

    }

    private void beginCraft(World world, BlockPos pos) {

        TileCrafter crafter = Util.getTileEntity(world, pos);
        assert crafter != null;

        if (world.isRemote) {
            ParticleItemDust.playCraftParticles(world, pos);
        }

        int durationTicks = crafter.size() * CRAFT_DURATION_MULTIPLIER * SCConstants.TICKS_PER_SECOND;

        crafter.scheduleCraft(world, this, durationTicks);


    }

    private void completeCrafting(World world, TileCrafter crafter) {

        crafter.stopCrafting();

        // Calculates the point at which the particle will end to decide where to drop the item.
        Vec3d center = crafter.centerOfHolograms();
        int durationTicks = crafter.size() * CRAFT_DURATION_MULTIPLIER * SCConstants.TICKS_PER_SECOND;
        double newY = center.y + (durationTicks - ParticleItemDust.PHASE_2_START_TICKS) * ParticleItemDust.PHASE_2_SPEED_BLOCKS_PER_TICK_UPWARDS;
        Vec3d endPos = new Vec3d(center.x, newY, center.z);

        // Find the correct recipe to craft with
        for (SpatialRecipe recipe : SpatialRecipe.getRecipes()) {
            if (recipe.matches(crafter.getHologramInvArr()) && !crafter.isCrafting()) {
                // Finally, drop the item on the ground.
                Util.dropItemStack(world, endPos, recipe.getOutput());
            }
        }

        // Removes the existing items
        Util.innerForEach(crafter.getHolograms(), blockPos -> {

            world.getTileEntity(blockPos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH).extractItem(0,
                    1,
                    false);

        });


    }

    /**
     * Called whenever scheduleBlockUpdate is called ON THE SERVER.
     */
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!state.getValue(TYPE).equals(UNFORMED)) {
            TileCrafter crafter = Util.getTileEntity(world, pos);
            assert crafter != null;

            if (crafter.isCrafting() && crafter.craftTimeHasPassed()) {
                completeCrafting(world, crafter);
                world.notifyBlockUpdate(crafter.masterPos(), state, state, SCConstants.NOTIFY_CLIENT);
            }
        }
    }



    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

        if (!state.getValue(TYPE).equals(UNFORMED)) {
            TileCrafter crafter = (TileCrafter) worldIn.getTileEntity(pos);
            assert crafter != null;

            // Stop ongoing animations
            if (worldIn.isRemote) {
                ParticleItemDust.stopParticles(crafter);
            }

            // Destroy all holograms in the BlockPos list.
            Util.innerForEach(crafter.getHolograms(), blockPos -> {
                if (worldIn.getBlockState(blockPos).getBlock() == SCBlocks.HOLOGRAM) {
                    worldIn.setBlockState(blockPos, Blocks.AIR.getDefaultState(), NOTIFY_CLIENT + BLOCK_UPDATE);
                }
            });


            // Notify the blocks they are no longer in a multiblock
            Util.innerForEach2D(crafter.getCrafterBlocks(), crafterPos -> {
                IBlockState blockState = worldIn.getBlockState(crafterPos);
                if (blockState.getBlock() instanceof BlockCrafter) {
                    worldIn.setBlockState(crafterPos, blockState.withProperty(TYPE, UNFORMED), SCConstants.NOTIFY_CLIENT);
                    worldIn.setTileEntity(crafterPos, null);
                }
            });


            WorldSavedDataCrafters.removeMasterBlock(worldIn, crafter.masterPos());


        }


        super.breakBlock(worldIn, pos, state);
    }

    private static List<BlockPos> getPossibleMultiblock(World world, BlockPos originalPos) {
        List<BlockPos> nearbyCrafters = new LinkedList<>();

        int crafterSize = ((BlockCrafter) (world.getBlockState(originalPos).getBlock())).size();
        BlockPos currentPos;

        // Put all nearby crafters in a list
        for (int i = -(crafterSize - 1); i <= crafterSize - 1; i++) {
            for (int j = -(crafterSize - 1); j <= crafterSize - 1; j++) {
                currentPos = originalPos.add(i, 0, j);

                // Adds if the other block is a crafter and of the same size.
                if (world.getBlockState(currentPos).getBlock().equals(world.getBlockState(originalPos).getBlock())) {
                    nearbyCrafters.add(currentPos);
                }

            }
        }

        List<BlockPos> list;

        // Try to form a multiblock from the existing crafters
        for (BlockPos nearbyCrafter1 : nearbyCrafters) {
            list = new LinkedList<>();
            list.add(nearbyCrafter1);
            for (BlockPos nearbyCrafter2 : nearbyCrafters) {

                if (nearbyCrafter2.equals(nearbyCrafter1)) {
                    continue;
                }

                if (validateMultiblockList(list, nearbyCrafter2, crafterSize)) {
                    list.add(nearbyCrafter2);
                }
                if (list.size() == crafterSize * crafterSize) {
                    return list;
                }
            }
        }

        return null;

    }

    private static boolean validateMultiblockList(List<BlockPos> list, BlockPos newValue, int crafterSize) {
        for (BlockPos oldValue : list) {
            if (Util.minimalDistanceOf(oldValue, newValue) > crafterSize - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean spaceExists(World world, List<BlockPos> startingPoses, int crafterSize) {

        for (BlockPos blockPos : startingPoses) {
            for (int i = 1; i <= crafterSize; i++) {
                if (!world.getBlockState(blockPos.add(0, i, 0)).getBlock().equals(Blocks.AIR)) {
                    return false;
                }
            }
        }


        return true;
    }

    @Override
    public Class<TileCrafter> getTileEntityClass() {
        return TileCrafter.class;
    }

    @Override
    public TileCrafter createTileEntity(World world, IBlockState state) {

        //TODO: remove MASTER blockstate
        if (state.getValue(TYPE).equals(FORMED)) {
            return new TileCrafter();
        } else {
            return null;
        }

    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BlockCrafter && ((BlockCrafter) other).crafterSize == this.crafterSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(crafterSize);
    }

    public enum CrafterType implements IStringSerializable {
        UNFORMED, FORMED, MASTER;

        @Override
        public String getName() {
            return this.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
//TODO: Add a way for the hologorams to 'strech' - 8 holograms means 8 crafting spaces, 27 holograms can be a 27 long snake or a 5x5 square with 2 left over, etc.
//TODO: Add a block placer for automatic crafting.





