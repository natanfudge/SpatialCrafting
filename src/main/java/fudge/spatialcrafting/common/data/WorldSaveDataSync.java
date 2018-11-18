package fudge.spatialcrafting.common.data;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketDebugPrint;
import fudge.spatialcrafting.network.client.PacketUpdateAllSharedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.List;

@Mod.EventBusSubscriber
public final class WorldSaveDataSync {

    private WorldSaveDataSync() {}

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.player);
    }


    private static void sync(EntityPlayer player) {
        World world = player.world;

        //TODO remove
      //  SpatialCrafting.LOGGER.error("Syncing in world " + world);

        if (!world.isRemote) {
            List<SharedData> data = WorldSavedDataCrafters.getAllData(world);
            PacketHandler.getNetwork().sendTo(new PacketUpdateAllSharedData(data), (EntityPlayerMP) player);

            if (SpatialCrafting.isDebugActive()) {
                PacketHandler.getNetwork().sendTo(new PacketDebugPrint(), (EntityPlayerMP) player);
            }

        }


    }


}
