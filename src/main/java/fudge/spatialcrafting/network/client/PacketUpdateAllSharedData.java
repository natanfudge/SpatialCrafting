package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.tile.util.CraftersData;
import fudge.spatialcrafting.common.tile.util.SharedData;
import io.netty.buffer.ByteBuf;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Packet from server to client
public class PacketUpdateAllSharedData implements IMessage {

    private static final String DATA_NBT = "data";
    private List<SharedData> allData;

    public PacketUpdateAllSharedData() {}


    public PacketUpdateAllSharedData(List<SharedData> data) {
        this.allData = data;
    }

    @Override
    public void toBytes(ByteBuf buffer) {

        // PacketBuffers are easier to use
        NBTTagCompound allSerializedData = new NBTTagCompound();

        for (int i = 0; i < allData.size(); i++) {
            SharedData data = allData.get(i);
            allSerializedData.setTag(DATA_NBT + i, data.serialized(new NBTTagCompound()));
        }

        ByteBufUtils.writeTag(buffer, allSerializedData);


    }

    @Override
    public void fromBytes(ByteBuf buffer) {

        NBTTagCompound allSerializedData = ByteBufUtils.readTag(buffer);
        if (allSerializedData != null) {
            Set<String> keys = allSerializedData.getKeySet();

            allData = new ArrayList<>();
            keys.forEach(key -> allData.add(new CraftersData(allSerializedData.getCompoundTag(key))));
        }


    }


    public static class Handler implements IMessageHandler<PacketUpdateAllSharedData, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketUpdateAllSharedData message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    WorldSavedDataCrafters.setAllData(world, message.allData);
                }
            }));

            return null;
        }
    }


}



