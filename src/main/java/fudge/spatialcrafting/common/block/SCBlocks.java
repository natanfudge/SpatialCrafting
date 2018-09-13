package fudge.spatialcrafting.common.block;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.TileHologram;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

import static fudge.spatialcrafting.common.util.Util.objectHolder;
import static net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(SpatialCrafting.MODID)
@Mod.EventBusSubscriber
public final class SCBlocks {


    @ObjectHolder(RegistryNames.HOLOGRAM)
    public static final BlockHologram HOLOGRAM = objectHolder();
    @ObjectHolder(RegistryNames.X2CRAFTER_BLOCK)
    public static final BlockCrafter X2CRAFTER_BLOCK = objectHolder();
    @ObjectHolder(RegistryNames.X3CRAFTER_BLOCK)
    public static final BlockCrafter X3CRAFTER_BLOCK = objectHolder();
    @ObjectHolder(RegistryNames.X4CRAFTER_BLOCK)
    public static final BlockCrafter X4CRAFTER_BLOCK = objectHolder();
    @ObjectHolder(RegistryNames.X5CRAFTER_BLOCK)
    public static final BlockCrafter X5CRAFTER_BLOCK = objectHolder();


    private static List<Block> blockList = ImmutableList.of(createBlock(new BlockHologram(), RegistryNames.HOLOGRAM),
            createBlock(new BlockCrafter(2), RegistryNames.X2CRAFTER_BLOCK),
            createBlock(new BlockCrafter(3), RegistryNames.X3CRAFTER_BLOCK),
            createBlock(new BlockCrafter(4), RegistryNames.X4CRAFTER_BLOCK),
            createBlock(new BlockCrafter(5), RegistryNames.X5CRAFTER_BLOCK));

    private SCBlocks() {}

    public static List<Block> getBlockList() {
        return blockList;
    }

    /**
     * Can only be used after registry
     */
    public static List<BlockCrafter> getAllCrafterBlocks() {
        return ImmutableList.of(X2CRAFTER_BLOCK, X3CRAFTER_BLOCK, X4CRAFTER_BLOCK, X5CRAFTER_BLOCK);
    }

    public static <T extends Block> T createBlock(T block, String name) {

        block.setTranslationKey(SpatialCrafting.MODID + '.' + name);

        // How fast the block breaks, this is the default in case I forget to add it especially for specific blocks or I don't care.
        block.setHardness(MCConstants.BASE_HARDNESS);

        // Name for Forge registry
        block.setRegistryName(name);

        return block;
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileHologram.class, HOLOGRAM.getRegistryName());
        GameRegistry.registerTileEntity(TileCrafter.class, X2CRAFTER_BLOCK.getRegistryName());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        blockList.forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        blockList.forEach(block -> registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
    }

    @UtilityClass
    public static class RegistryNames {

        public static final String HOLOGRAM = "hologram";
        public static final String X2CRAFTER_BLOCK = "x2crafter_block";
        public static final String X3CRAFTER_BLOCK = "x3crafter_block";
        public static final String X4CRAFTER_BLOCK = "x4crafter_block";
        public static final String X5CRAFTER_BLOCK = "x5crafter_block";


    }


}
