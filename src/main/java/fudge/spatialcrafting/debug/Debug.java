package fudge.spatialcrafting.debug;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.SharedData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Mod.EventBusSubscriber
public final class Debug {

   private Debug(){}

    public static String getAllCrafterData(World world, boolean client) {
        List<SharedData> allData = WorldSavedDataCrafters.getAllData(world);

        StringBuilder string = new StringBuilder();


        allData.forEach(dataG -> {
            CraftersData data = (CraftersData) dataG;
            String worldType = client ? "CLIENT" : "SERVER";
            string.append(String.format("%n**** Data for crafter multiblock at masterPos = %s in %s world ****%n",
                    data.getMasterPos(),
                    worldType));
            string.append(String.format("Craft End Time = %s %n", data.getCraftTime() != 0 ? data.getCraftTime() : "not crafting"));
            string.append(String.format("Recipe = %s %n", data.getRecipe() != null ? data.getRecipe() : "No help recipe active"));

        });

        return string.toString();
    }

    @SideOnly(Side.CLIENT)
    public static void printClientDebug(){
        SpatialCrafting.LOGGER.info(Debug.getAllCrafterData(Minecraft.getMinecraft().world, true));
    }

    @SubscribeEvent
    public static void debugOnWorldLoad(WorldEvent.Load event) {
        if (SpatialCrafting.isDebugActive() && !event.getWorld().isRemote) {
            String debugInfo = getAllCrafterData(event.getWorld(), false);
            if (!debugInfo.equals("")) {
                SpatialCrafting.LOGGER.info(getAllCrafterData(event.getWorld(), false));
            }
        }
    }


}