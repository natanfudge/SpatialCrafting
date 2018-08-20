package fudge.spatialcrafting.common.data;

import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.PacketUpdateWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;

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
        if (!world.isRemote) {
            Map<BlockPos, Long> data = WorldSavedDataCrafters.getData(world);
            PacketHandler.getNetwork().sendTo(new PacketUpdateWorldSavedData(data), (EntityPlayerMP) player);
        }
    }


}
