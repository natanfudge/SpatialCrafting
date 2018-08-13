package fudge.spatialcrafting.client.gui;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.block.SCBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class SpatialCraftingTab extends CreativeTabs {


    public SpatialCraftingTab() {
        super(SpatialCrafting.MODID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(SCBlocks.X2CRAFTER_BLOCK);
    }

}
