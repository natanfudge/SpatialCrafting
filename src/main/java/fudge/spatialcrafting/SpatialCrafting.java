package fudge.spatialcrafting;


import fudge.spatialcrafting.client.gui.SpatialCraftingTab;
import fudge.spatialcrafting.common.command.Commands;
import fudge.spatialcrafting.compat.crafttweaker.CraftTweakerIntegration;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.proxy.IProxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fudge.spatialcrafting.common.block.SCBlocks.registerTileEntities;


@Mod(modid = SpatialCrafting.MODID, name = SpatialCrafting.NAME, version = SpatialCrafting.VERSION, dependencies = "required-after:forge@[14.23.1.2577,);" + "required-after:crafttweaker;")
public class SpatialCrafting {

    public static final String MODID = "spatialcrafting";
    public static final String NAME = "Spatial Crafting";
    public static final String VERSION = "0.0.0";
    public static final SpatialCraftingTab SPATIAL_CRAFTING_TAB = new SpatialCraftingTab();

    public static final Logger LOGGER = LogManager.getLogger("Spatial Crafting");
    private static final String SERVER_PROXY_PATH = "fudge.spatialcrafting.proxy.ServerProxy";
    private static final String CLIENT_PROXY_PATH = "fudge.spatialcrafting.proxy.ClientProxy";
    @Mod.Instance(MODID)
    private static SpatialCrafting instance;
    @SuppressWarnings("unused")
    @SidedProxy(serverSide = SERVER_PROXY_PATH, clientSide = CLIENT_PROXY_PATH)
    private static IProxy proxy;

    public static SpatialCrafting getInstance() {
        return instance;
    }

    public static IProxy getProxy() {
        return proxy;
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        PacketHandler.registerPackets();


        if (Loader.isModLoaded("crafttweaker")) {
            CraftTweakerIntegration.init();
        }


        proxy.preInit();

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        registerTileEntities();

        proxy.init();

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new Commands());
    }


}
