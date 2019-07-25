package spatialcrafting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;



public class MyBlock extends Block {
    public static final BooleanProperty MyBlockIsHard = BooleanProperty.of("is_hard");


    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> stateFactory) {
        stateFactory.add(MyBlockIsHard);
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        world.setBlockState(pos, MyBlocks.INSTANCE.getMY_BLOCK_INSTANCE().getDefaultState().with(MyBlockIsHard, true));
        return true;
    }



    @Override
    public float getHardness(BlockState blockState, BlockView blockView, BlockPos pos) {
        boolean isHard = blockState.get(MyBlockIsHard);
        if(isHard) return 2.0f;
        else return 0.5f;
    }

    public MyBlock(Settings block$Settings_1) {
        super(block$Settings_1);
        setDefaultState(getStateFactory().getDefaultState().with(MyBlockIsHard, false));
    }
}