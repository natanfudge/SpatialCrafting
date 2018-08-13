package fudge.spatialcrafting.client;


import fudge.spatialcrafting.common.block.SCBlocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// For registering client-side only stuff with events
@SuppressWarnings("NullableProblems")
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class SCClientRegistry {

    private SCClientRegistry() { }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // Register itemBlock models
        SCBlocks.getBlockList().forEach(block -> {
            Item itemBlock = Item.getItemFromBlock(block);
            ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
        });

    }


}
