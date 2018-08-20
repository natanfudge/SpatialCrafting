package fudge.spatialcrafting.common.block;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.SCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.TileHologram;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialTransparent;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;


@SuppressWarnings("deprecation")
public class BlockHologram extends BlockTileEntity<TileHologram> {


    private static final Material HOLOGRAM = new MaterialTransparent(MapColor.AIR);

    public BlockHologram() {
        super(HOLOGRAM);
    }


    /**
     * Doesn't explicitly make it see-through, but required for things such as making light pass through it.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
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
        return SCConstants.UNBREAKABLE;
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return SCConstants.INDESTRUCTIBLE;
    }


    //TODO: fix bug with stuff sometimes not inserting server side
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        try {
            TileHologram tile = Util.getTileEntity(world, pos);
            TileCrafter crafter = tile.getCrafter();
            IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            ItemStack heldItem = player.getHeldItem(hand);
            if (!player.isSneaking()) {
                // Inputs the held item into the hologram / takes it out of the hologram and gives it back to the player
                if (!heldItem.isEmpty()) {
                    if (!crafter.isCrafting()) {
                        // Put item into the hologram
                        ItemStack remainingItemStack = itemHandler.insertItem(0, heldItem, false);
                        if (!player.isCreative()) {
                            player.setHeldItem(hand, remainingItemStack);
                        }
                    }

                } else {
                    // Take item out of the hologram
                    ItemStack extractedItemStack = itemHandler.extractItem(0, SCConstants.NORMAL_ITEMSTACK_LIMIT, false);
                    ItemHandlerHelper.giveItemToPlayer(player, extractedItemStack);

                    // If items were taken out during crafting then it must be stopped
                    if (extractedItemStack.getCount() >= 1 && crafter.isCrafting()) {
                        crafter.stopCrafting();
                    }

                }

            }


        } catch (Exception e) {
            SpatialCrafting.LOGGER.error("Exception caught in BlockHologram::onBlockActivated", e);
        }
        return true;
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileHologram tile = Util.getTileEntity(world, pos);
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
        ItemStack itemStack = itemHandler.getStackInSlot(0);

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
