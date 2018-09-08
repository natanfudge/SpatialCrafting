package fudge.spatialcrafting.common.block;

import fudge.spatialcrafting.client.particle.ParticleItemDust;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.TileHologram;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;


@SuppressWarnings("deprecation")
public class BlockHologram extends BlockTileEntity<TileHologram> {


    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    private static final Material HOLOGRAM = new MaterialTransparent(MapColor.AIR);

    public BlockHologram() {
        super(HOLOGRAM);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
    }


    /**
     * Doesn't explicitly make it see-through, but required for things such as making light pass through it.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(ACTIVE)) {
            return super.getBoundingBox(state, source, pos);
        } else {
            return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        }

    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(ACTIVE)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == 1) {
            return this.getDefaultState().withProperty(ACTIVE, true);
        } else {
            return this.getDefaultState().withProperty(ACTIVE, false);
        }
    }

    /**
     * Makes it see-through
     */
    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }


    /**
     * @return NULL_AABB (null collision box) if you can pass through it, FULL_BLOCK_AABB if you can't.
     */
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    /**
     * @return true if the block can be interacted with (broken/activated), false otherwise.
     */
    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return true;
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        // Unbreakable
        return MCConstants.UNBREAKABLE;
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return MCConstants.INDESTRUCTIBLE;
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!state.getValue(ACTIVE)) return false;


        TileHologram hologramTile = Util.getTileEntity(world, pos);
        TileCrafter crafter = hologramTile.getCrafter();
        ItemStack heldItem = player.getHeldItem(hand);

        if (!player.isSneaking()) {
            // Inputs the held item into the hologram / takes it out of the hologram and gives it back to the player

            if (heldItem.isEmpty()) {
                extractItem(world, player, crafter, hologramTile);

                // If there was nothing in there, so we should put an item in there in the case that the player is holding an item.
            } else {
                if (!crafter.isCrafting()) {
                    // Stop displaying ghost item. Note that this must be done BEFORE inserting or no item will be inserted.
                    hologramTile.stopDisplayingGhostItem();

                    // Put item into the hologram
                    ItemStack remainingItemStack = hologramTile.insertItem(heldItem);


                    if (!player.isCreative()) {
                        player.setHeldItem(hand, remainingItemStack);
                    }
                }


            }

            if (crafter.isHelpActive()) {
                crafter.proceedHelp();
            }


            return true;
        }
        return false;
    }

    private void extractItem(World world, EntityPlayer player, TileCrafter crafter, TileHologram hologram) {
        // Take item out of the hologram
        ItemStack extractedItemStack = hologram.extractItem(MCConstants.NORMAL_ITEMSTACK_LIMIT);
        ItemHandlerHelper.giveItemToPlayer(player, extractedItemStack);

        // If items were taken out during crafting then it must be stopped
        if (extractedItemStack.getCount() >= 1 && crafter.isCrafting()) {
            crafter.resetCraftingState();
            if (world.isRemote) {
                ParticleItemDust.stopParticles(crafter);
            }
        }
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        checkForItemExtraction(world, pos, player);
    }

    private void checkForItemExtraction(World world, BlockPos pos, EntityPlayer player) {
        TileHologram hologramTile = Util.getTileEntity(world, pos);
        TileCrafter crafter = hologramTile.getCrafter();

        if (!player.isSneaking()) {
            extractItem(world, player, crafter, hologramTile);
        }
    }


    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        //Edge case when a hologram is left clicked in creative. Neither breakBlock or onBlockClicked is called.
        checkForItemExtraction(world, pos, player);

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileHologram tile = Util.getTileEntity(world, pos);

        ItemStack itemStack = tile.getStoredItem();

        // Drops the item in the hologram (if it exists) on the ground.
        if (!itemStack.isEmpty()) {
            Util.dropItemStack(world, new Vec3d(pos), itemStack);
        }

        super.breakBlock(world, pos, state);

    }

    @Override
    public Class<TileHologram> getTileEntityClass() {
        return TileHologram.class;
    }

    @Override
    public TileHologram createTileEntity(World world, IBlockState state) {
        return new TileHologram();
    }

}
