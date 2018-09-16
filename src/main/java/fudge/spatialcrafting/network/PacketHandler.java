package fudge.spatialcrafting.network;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.network.client.*;
import fudge.spatialcrafting.network.server.PacketSetActiveLayer;
import fudge.spatialcrafting.network.server.PacketStartCraftingHelp;
import fudge.spatialcrafting.network.server.PacketStopCraftingHelp;
import lombok.NoArgsConstructor;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@NoArgsConstructor
public class PacketHandler {
    private static SimpleNetworkWrapper network;
    private static int packetId = 0;

    private static int nextID() {
        return packetId++;
    }

    public static void registerPackets() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(SpatialCrafting.MODID);

        registerPacket(new PacketUpdateAllSharedData.Handler(), PacketUpdateAllSharedData.class, true);
        registerPacket(new PacketStopParticles.Handler(), PacketStopParticles.class, true);
        registerPacket(new PacketAttemptMultiblock.Handler(), PacketAttemptMultiblock.class, true);
        registerPacket(new PacketRemoveMasterBlock.Handler(), PacketRemoveMasterBlock.class, true);
        registerPacket(new PacketRemoveTileEntity.Handler(), PacketRemoveTileEntity.class, true);
        registerPacket(new PacketDebugPrint.Handler(), PacketDebugPrint.class, true);

        registerPacket(new PacketStartCraftingHelp.Handler(), PacketStartCraftingHelp.class, false);
        registerPacket(new PacketSetActiveLayer.Handler(), PacketSetActiveLayer.class, false);
        registerPacket(new PacketStopCraftingHelp.Handler(), PacketStopCraftingHelp.class, false);


    }

    private static <REQ extends IMessage, REPLY> void registerPacket(IMessageHandler<REQ, ? extends REPLY> handler, Class<REQ> packetClass, boolean toClient) {
        network.registerMessage(handler, packetClass, nextID(), toClient ? Side.CLIENT : Side.SERVER);
    }

    public static SimpleNetworkWrapper getNetwork() {
        return network;
    }

}