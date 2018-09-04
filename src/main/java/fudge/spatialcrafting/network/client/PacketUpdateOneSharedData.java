package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.SharedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * Packet from server to client
 * Hardcoded to CrafterData
 **/
public class PacketUpdateOneSharedData implements IMessage {

    private static final String DATA_NBT = "data";
    private SharedData data;


    public PacketUpdateOneSharedData(SharedData data) {
        this.data = data;
    }


    public PacketUpdateOneSharedData() {
        // Necessary for reflection
    }

    @Override
    public void toBytes(ByteBuf buffer) {

        // PacketBuffers are easier to use
        PacketBuffer wrappedBuffer = new PacketBuffer(buffer);

        wrappedBuffer.writeCompoundTag(data.serialized(new NBTTagCompound()));


    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer wrappedBuffer = new PacketBuffer(buffer);

        try {
            NBTTagCompound serializedData = wrappedBuffer.readCompoundTag();
            data = new CraftersData(serializedData);


        } catch (IOException e) {
            SpatialCrafting.LOGGER.error(e);
        }

    }


    public static class Handler implements IMessageHandler<PacketUpdateOneSharedData, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketUpdateOneSharedData message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    WorldSavedDataCrafters.setOneData(world, message.data);
                }
            }));

            return null;
        }
    }


}



