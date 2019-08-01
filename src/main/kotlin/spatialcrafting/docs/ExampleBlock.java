package spatialcrafting.docs;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Step 1: get .ogg file
// Step 2: put sounds.json
// Step 3: create sound event
// Step 4: register
// Step 5: call playSound
// Step 6: troubleshooting: remove out folder, turn on sounds.

public class ExampleBlock extends Block {
    public ExampleBlock() {
        super(Settings.of(Material.STONE));
    }

    @Override
    public boolean activate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity placedBy, Hand hand, BlockHitResult blockHitResult) {
        if (!world.isClient) {
            world.playSound(
                    null, // Player (purpose unknown, edit if you know)
                    blockPos, // The position of where the sound will come from
                    ExampleMod.MY_SOUND_EVENT, // The sound that will play
                    SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                    1f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
                    1f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
            );
        }

//        if (!world.isClient) {
//            world.playSound(
//                    null, // Player (purpose unknown, edit if you know)
//                    blockPos, // The position of where the sound will come from
//                    SoundEvents.BLOCK_ANVIL_LAND, // The sound that will play
//                    SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
//                    1f, //Volume multiplier, 1 is normal, 0.5 is half volume, etc
//                    1f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
//            );
//        }

        return false;
    }
}
