package fudge.spatialcrafting.debug;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.SharedData;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.List;

@Mod.EventBusSubscriber
public class Debug {

   /* public static void printAllCrafterData(World world) {
        List<SharedData> allData = WorldSavedDataCrafters.getAllData(world);

        StringBuilder string = new StringBuilder();


        allData.forEach(dataG -> {
            CraftersData data = (CraftersData) dataG;
            String worldType = world.isRemote ? "CLIENT" : "SERVER";
            SpatialCrafting.LOGGER.info("**** Data for crafter multiblock at masterPos = {} in {} world ****", data.getMasterPos(),worldType);
            SpatialCrafting.LOGGER.info("Craft End Time = {} ", data.getCraftTime() != 0 ? data.getCraftTime() : "not crafting");
            SpatialCrafting.LOGGER.info("Recipe = {} \n", data.getRecipe() != null ? data.getRecipe() : "No help recipe active");

        });
    }*/

    public static String getAllCrafterData(World world, boolean client) {
        List<SharedData> allData = WorldSavedDataCrafters.getAllData(world);

        StringBuilder string = new StringBuilder();


        allData.forEach(dataG -> {
            CraftersData data = (CraftersData) dataG;
            String worldType = client ? "CLIENT" : "SERVER";
            string.append(String.format("\n**** Data for crafter multiblock at masterPos = %s in %s world ****\n",
                    data.getMasterPos(),
                    worldType));
            string.append(String.format("Craft End Time = %s \n", data.getCraftTime() != 0 ? data.getCraftTime() : "not crafting"));
            string.append(String.format("Recipe = %s \n", data.getRecipe() != null ? data.getRecipe() : "No help recipe active"));

        });

        return string.toString();
    }

    @SubscribeEvent
    public static void debugOnWorldLoad(WorldEvent.Load event) {
        if (SpatialCrafting.debugActive && !event.getWorld().isRemote) {
            String debugInfo = getAllCrafterData(event.getWorld(), false);
            if (!debugInfo.equals("")) {
                SpatialCrafting.LOGGER.info(getAllCrafterData(event.getWorld(), false));
            }
        }
    }



}
