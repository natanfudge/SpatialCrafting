package fudge.spatialcrafting.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Config.LangKey;

import static fudge.spatialcrafting.SpatialCrafting.MODID;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Comment;

@Mod.EventBusSubscriber
public class ScConfig {


    @LangKey("spatialcrafting.config.power")
    @Config(modid = MODID)
    public static class General
    {
        @Config.Name("Crafting requires Forge Energy")
        public static boolean requireEnergy = false;


    }

    @SubscribeEvent
    public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)){
            ConfigManager.sync(MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
        }
    }
}
