package fudge.spatialcrafting.client.sound;

import fudge.spatialcrafting.SpatialCrafting;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@UtilityClass
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class Sounds {
    public static SoundEvent CRAFT_END;
    public static SoundEvent CRAFT_START;
    public static SoundEvent CRAFT_LOOP;



    @SubscribeEvent
    public static void onSoundRegistry(RegistryEvent.Register<SoundEvent> event){
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        CRAFT_END = registerSound(RegistryNames.CRAFT_END, registry);
        CRAFT_START = registerSound(RegistryNames.CRAFT_START,registry);
        CRAFT_LOOP = registerSound(RegistryNames.CRAFT_LOOP, registry);

    }

    private static SoundEvent registerSound(String name, IForgeRegistry<SoundEvent> registry){
        SoundEvent sound = new SoundEvent(new ResourceLocation(SpatialCrafting.MODID,name));
        sound.setRegistryName(name);
        registry.register(sound);
        return sound;
    }


    @UtilityClass
    public static class RegistryNames{
        public static String CRAFT_END = "craft_end";
        public static String CRAFT_LOOP = "craft_loop";
        public static String CRAFT_START = "craft_start";
    }

}
