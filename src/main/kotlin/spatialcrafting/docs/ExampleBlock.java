package spatialcrafting.docs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class ExampleBlock extends Block implements BlockEntityProvider {

    public ExampleBlock() {
        super(Settings.of(Material.STONE));
    }


    @Override
    public BlockEntity createBlockEntity(BlockView var1) {
        return new DemoBlockEntity();
    }
}



//        if (!stack.isEmpty) {
//            val minecraft = MinecraftClient.getInstance()
//
//            // Must be done for all GL calls
//            GlStateManager.pushMatrix()
//
//            val time = tile.world!!.time + partialTicks
//
//            // Changes the position of the item to float up and down like a sine wave.
//            val offset = sin((time - tile.lastChangeTime) * OFFSET_CHANGE_SPEED_MULTIPLIER) * OFFSET_AMOUNT_MULTIPLIER
//            GlStateManager.translated(x + MOVE_TO_MID_BLOCK_OFFSET, y + offset + HEIGHT_INCREASE, z + MOVE_TO_MID_BLOCK_OFFSET)
//
//            // Makes the item bigger
//            GlStateManager.scalef(SIZE_MULTIPLIER.toFloat(), SIZE_MULTIPLIER.toFloat(), SIZE_MULTIPLIER.toFloat())
//
//            // Spins the item around
//            GlStateManager.rotatef(time, 0f, SPIN_SPEED.toFloat(), 0.toFloat())
//            minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND)
//
//            // Enabled GL stuff must be disabled after.
//            GlStateManager.scalef(1f, 1f, 1f)
//            GlStateManager.popMatrix()
//        }
//    }

