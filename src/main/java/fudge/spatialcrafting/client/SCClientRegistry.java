package fudge.spatialcrafting.client;


import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.SCBlocks;
import lombok.experimental.UtilityClass;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

// For registering client-side only stuff with events
@SuppressWarnings("NullableProblems")
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
@UtilityClass
public class SCClientRegistry {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // Register itemBlock models
        SCBlocks.getBlockList().forEach(block -> {
            Item itemBlock = Item.getItemFromBlock(block);
            ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
        });

    }





}
