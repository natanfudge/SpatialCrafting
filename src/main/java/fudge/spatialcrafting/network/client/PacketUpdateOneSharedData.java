package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.SharedData;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Packet from server to client
 * Hardcoded to CrafterData
 **/
//TODO: this is not used at all?...
@NoArgsConstructor
public class PacketUpdateOneSharedData implements IMessage {

    private SharedData data;


    public PacketUpdateOneSharedData(SharedData data) {
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, data.serialized(new NBTTagCompound()));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        NBTTagCompound compound = ByteBufUtils.readTag(buffer);
        assert compound != null;
        data = new CraftersData(compound);
    }


    public static class Handler implements IMessageHandler<PacketUpdateOneSharedData, IMessage> {

        @Override
        @Nullable
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



