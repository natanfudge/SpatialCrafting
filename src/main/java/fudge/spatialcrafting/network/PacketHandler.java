package fudge.spatialcrafting.network;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.network.block.PacketUpdateHologram;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public class PacketHandler {
    private static SimpleNetworkWrapper network = null;
    private static int packetId = 0;

    public PacketHandler() {
    }

    private static int nextID() {
        return packetId++;
    }

    public static void registerPackets() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(SpatialCrafting.MODID);
        network.registerMessage(new PacketUpdateHologram.Handler(), PacketUpdateHologram.class, nextID(), Side.CLIENT);

    }

    public static SimpleNetworkWrapper getNetwork() {
        return network;
    }

}