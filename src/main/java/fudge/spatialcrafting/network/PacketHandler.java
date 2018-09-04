package fudge.spatialcrafting.network;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.network.client.*;
import fudge.spatialcrafting.network.server.PacketSetActiveLayer;
import fudge.spatialcrafting.network.server.PacketStartCraftingHelp;
import fudge.spatialcrafting.network.server.PacketStopCraftingHelp;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public class PacketHandler {
    private static SimpleNetworkWrapper network = null;
    private static int packetId = 0;

    public PacketHandler() {
        // Required for reflection
    }

    private static int nextID() {
        return packetId++;
    }

    public static void registerPackets() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(SpatialCrafting.MODID);
        network.registerMessage(new PacketUpdateAllSharedData.Handler(), PacketUpdateAllSharedData.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketStopParticles.Handler(), PacketStopParticles.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketAttemptMultiblock.Handler(), PacketAttemptMultiblock.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketRemoveMasterBlock.Handler(), PacketRemoveMasterBlock.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketUpdateOneSharedData.Handler(), PacketUpdateOneSharedData.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketRemoveTileEntity.Handler(), PacketRemoveTileEntity.class, nextID(), Side.CLIENT);
        network.registerMessage(new PacketDebugPrint.Handler(), PacketDebugPrint.class, nextID(), Side.CLIENT);

        network.registerMessage(new PacketStartCraftingHelp.Handler(), PacketStartCraftingHelp.class, nextID(), Side.SERVER);
        network.registerMessage(new PacketSetActiveLayer.Handler(), PacketSetActiveLayer.class, nextID(), Side.SERVER);
        network.registerMessage(new PacketStopCraftingHelp.Handler(), PacketStopCraftingHelp.class, nextID(), Side.SERVER);



    }

    public static SimpleNetworkWrapper getNetwork() {
        return network;
    }

}