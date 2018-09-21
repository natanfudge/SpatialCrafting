package fudge.spatialcrafting.common.block;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.client.sound.Sounds;
import fudge.spatialcrafting.client.util.ParticleUtil;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.util.CraftingInventory;
import fudge.spatialcrafting.common.tile.util.CubeArr;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.common.util.MCConstants;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.NetworkUtil;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketAttemptMultiblock;
import kotlin.Unit;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static fudge.spatialcrafting.common.util.MCConstants.BLOCK_UPDATE;
import static fudge.spatialcrafting.common.util.MCConstants.NOTIFY_CLIENT;


@SuppressWarnings("deprecation")
public class BlockCrafter extends BlockTileEntity<TileCrafter> {


    public static final int CRAFT_DURATION_MULTIPLIER = 5;

    public static final PropertyBool FORMED = PropertyBool.create("formed");
    private final int crafterSize;

    public BlockCrafter(int size) {
        super(Material.WOOD);

        this.crafterSize = size;
        this.setDefaultState(this.blockState.getBaseState().withProperty(FORMED, false));
        this.setHardness(2.0F);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(SpatialCrafting.SPATIAL_CRAFTING_TAB);

    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TextComponentTranslation(String.format("tile.spatialcrafting.x%dcrafter_block.info",
                size())).getUnformattedComponentText());
    }

    public int size() {
        return crafterSize;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FORMED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == 0) {
            return this.getDefaultState().withProperty(FORMED, false);
        } else {
            return this.getDefaultState().withProperty(FORMED, true);
        }

    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(FORMED)) {
            return 0;
        } else {
            return 1;
        }

    }

    /**
     * Called when the block is placed by any source.
     */
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        // onBlockPlacedBy(worldIn, pos, state, null, null);
        // Might not work when placed by a machine
    }

    /**
     * Called when a block is placed by something.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        CrafterUtil.attemptMultiblock(world, pos, placer, size());
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!state.getValue(FORMED)) {
            CrafterUtil.attemptMultiblock(world, pos, null, size());

            // Send this to the client
            PacketHandler.getNetwork().sendToAllAround(new PacketAttemptMultiblock(pos), NetworkUtil.INSTANCE.createTargetPoint(world, pos));

        }


    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        try {
            if (state.getValue(FORMED) && playerIn.getHeldItem(hand).isEmpty()) {
                TileCrafter crafter = Util.getTileEntity(world, pos);

                CraftingInventory craftingInventory = crafter.getHologramInvArr();

                if (!crafter.isCrafting()) {
                    // Check if any recipe matches, if so, beginCraft the recipe.
                    for (SpatialRecipe recipe : SpatialRecipe.getRecipes()) {
                        if (recipe.matches(craftingInventory)) {
                            beginCraft(world, pos, recipe);
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

    //TODO make the dropped item allign better. maybe  float


    private void beginCraft(World world, BlockPos pos, SpatialRecipe recipe) {

        TileCrafter crafter = Util.getTileEntity(world, pos);

        int durationTicks = recipe.size() * CRAFT_DURATION_MULTIPLIER * MCConstants.TICKS_PER_SECOND;

        if (world.isRemote) {
            ParticleUtil.playCraftParticles(world, pos, durationTicks);
        } else {
            // Normal sound can be done through the server
            world.playSound(null, pos, Sounds.CRAFT_START, SoundCategory.BLOCKS, 0.8f, 0.8f);
        }


        crafter.scheduleCraft(world, durationTicks);

        crafter.activateAllLayers();


    }


    @Override
    public void breakBlock(World world, BlockPos placedPos, IBlockState state) {

        if (state.getValue(FORMED)) {
            TileCrafter crafter = Util.getTileEntity(world, placedPos);

            destroyMultiblock(world, crafter);


        }


        super.breakBlock(world, placedPos, state);
    }

    private void destroyMultiblock(World world, TileCrafter crafter) {

        if (crafter.isCrafting()) {
            crafter.resetCraftingState(true);
        }

        // Destroy all holograms in the BlockPos list.
        CubeArr<BlockPos> holograms = crafter.getHolograms();

        crafter.getHolograms().forEach(hologramPos -> {
            if (world.getBlockState(hologramPos).getBlock() == SCBlocks.HOLOGRAM) {
                world.setBlockState(hologramPos, Blocks.AIR.getDefaultState(), NOTIFY_CLIENT + BLOCK_UPDATE);
            }
        });


        // Notify the blocks they are no longer in a multiblock
        crafter.getCrafterBlocks().forEach(crafterPos -> {
            IBlockState blockState = world.getBlockState(crafterPos);
            if (blockState.getBlock() instanceof BlockCrafter) {
                world.setBlockState(crafterPos, blockState.withProperty(FORMED, false), MCConstants.NOTIFY_CLIENT);
                Util.removeTileEntity(world, crafterPos, true);
            }

            return Unit.INSTANCE;
        });

        WorldSavedDataCrafters.removeData(world, crafter.masterPos(), true);


    }

    @Override
    public Class<TileCrafter> getTileEntityClass() {
        return TileCrafter.class;
    }

    @Override
    public TileCrafter createTileEntity(World world, IBlockState state) {

        if (state.getValue(FORMED)) {
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


}
//TODO: Add a way for the hologorams to 'strech' - 8 holograms means 8 crafting spaces, 27 holograms can be a 27 long snake or a 5x5 square with 2 left over, etc.
//TODO: Add a block placer for automatic crafting.





