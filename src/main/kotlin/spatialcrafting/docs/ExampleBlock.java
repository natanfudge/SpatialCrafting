package spatialcrafting.docs;


import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;


public class ExampleBlock extends Block implements BlockEntityProvider {
    public ExampleBlock() {
        super(Settings.of(Material.STONE));
    }


    @Override
    public boolean activate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        if (world.isClient) return true;
        Inventory blockEntity = (Inventory) world.getBlockEntity(blockPos);


        if (!player.getStackInHand(hand).isEmpty()) {
            // Check what is the first open slot and put an item from the player's hand there
            if (blockEntity.getInvStack(0).isEmpty()) {
                // Put the stack the player is holding into the inventory
                blockEntity.setInvStack(0, player.getStackInHand(hand).copy());
                // Remove the stack from the player's hand
                player.getStackInHand(hand).setCount(0);
            } else if (blockEntity.getInvStack(1).isEmpty()) {
                blockEntity.setInvStack(1, player.getStackInHand(hand).copy());
                player.getStackInHand(hand).setCount(0);
            } else {
                // If the inventory is full we'll print it's contents
                System.out.println("The first slot holds "
                        + blockEntity.getInvStack(0) + " and the second slot holds " + blockEntity.getInvStack(1));
            }
        } else {
            // If the player is not holding anything we'll get give him the items in the block entity one by one

            // Find the first slot that has an item and give it to the player
            if (!blockEntity.getInvStack(1).isEmpty()) {
                // Give the player the stack in the inventory
                player.inventory.offerOrDrop(world, blockEntity.getInvStack(1));
                // Remove the stack from the inventory
                blockEntity.removeInvStack(1);
            } else if (!blockEntity.getInvStack(0).isEmpty()) {
                player.inventory.offerOrDrop(world, blockEntity.getInvStack(0));
                blockEntity.removeInvStack(0);
            }
        }


        return true;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView var1) {
        return new DemoBlockEntity();
    }

}
