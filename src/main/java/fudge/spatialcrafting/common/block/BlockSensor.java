package fudge.spatialcrafting.common.block;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static fudge.spatialcrafting.common.SCConstants.NOTIFY_CLIENT;


@SuppressWarnings("deprecation")
public class BlockSensor extends Block {

    public static final PropertyBool ON = PropertyBool.create("on");


    public BlockSensor() {
        super(Material.ROCK);

        setDefaultState(this.blockState.getBaseState().withProperty(ON, false));
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ON);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == 0) {
            return this.getDefaultState().withProperty(ON, false);
        } else {
            return this.getDefaultState().withProperty(ON, true);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {

        if (state.getValue(ON)) {
            return 1;
        } else {
            return 0;
        }

    }


    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {


        Block block = worldIn.getBlockState(pos.up()).getBlock();


        // Block was placed/changed
        if (!block.equals(Blocks.AIR)) {
            worldIn.setBlockState(pos, state.withProperty(ON, true), NOTIFY_CLIENT);

            // Block was removed
        } else {
            worldIn.setBlockState(pos, state.withProperty(ON, false), NOTIFY_CLIENT);
        }

    }

    @Override
    public int getLightValue(IBlockState state) {
        if (state.getValue(ON)) {
            return 15;      // Magic numbers are the sad state of metadatas
        } else {
            return 0;
        }

    }


    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.neighborChanged(state, worldIn, pos, this, pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return super.getRenderLayer();
    }


}

