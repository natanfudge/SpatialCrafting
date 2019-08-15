package spatialcrafting.docs;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {
    public static ExampleBlock EXAMPLE_BLOCK = new ExampleBlock();
    private static final String MY_SOUND_ID = "tutorial:my_sound";
    public static SoundEvent MY_SOUND_EVENT = new SoundEvent(new Identifier(MY_SOUND_ID));
    public static BlockEntityType<DemoBlockEntity> DEMO_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create(DemoBlockEntity::new, EXAMPLE_BLOCK).build(null);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("tutorial:example_block"), ExampleMod.EXAMPLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("tutorial:example_block"), new BlockItem(EXAMPLE_BLOCK, new Item.Settings()));
        Registry.register(Registry.SOUND_EVENT, MY_SOUND_ID, MY_SOUND_EVENT);
        Registry.register(Registry.BLOCK_ENTITY, "tutorial:demo", DEMO_BLOCK_ENTITY_TYPE);
    }
}
