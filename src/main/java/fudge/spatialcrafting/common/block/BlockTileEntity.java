package fudge.spatialcrafting.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class BlockTileEntity<T extends TileEntity> extends Block {

    public BlockTileEntity(Material material) {
        super(material);
    }

    public abstract Class<T> getTileEntityClass();

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public abstract T createTileEntity(World world, IBlockState state);
}


