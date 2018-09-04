package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.debug.Debug;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


// Packet from server to client
public class PacketDebugPrint implements IMessage {


    public PacketDebugPrint() {
        // Necessary for reflection
    }

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}


    public static class Handler implements IMessageHandler<PacketDebugPrint, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketDebugPrint message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    SpatialCrafting.LOGGER.info(Debug.getAllCrafterData(Minecraft.getMinecraft().world, true));
                }
            }));

            return null;
        }
    }


}



