package fudge.spatialcrafting.network;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// Packet from server to client
public class PacketUpdateAllSharedData implements IMessage {

    private Map<BlockPos, Long> craftEndTimes;


    public PacketUpdateAllSharedData(Map<BlockPos, Long> craftEndTimes) {
        this.craftEndTimes = craftEndTimes;
    }


    // Necessary for reflection
    public PacketUpdateAllSharedData() {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        // PacketBuffers are easier to use
        PacketBuffer wrappedBuffer = new PacketBuffer(buffer);

        // Serialize Map to NBT
        NBTTagCompound serializedMap = new NBTTagCompound();
        craftEndTimes.forEach((pos, time) -> serializedMap.setLong(Long.toString(pos.toLong()), time));
        // Serialize NBT to ByteBuf
        wrappedBuffer.writeCompoundTag(serializedMap);


    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer wrappedBuffer = new PacketBuffer(buffer);

        try {
            NBTTagCompound serializedMap = wrappedBuffer.readCompoundTag();

            Set<String> keys = serializedMap.getKeySet();

            craftEndTimes = new HashMap<>();
            keys.forEach(key -> craftEndTimes.put(BlockPos.fromLong(Long.parseLong(key)), Long.parseLong(key)));

        } catch (IOException e) {
            SpatialCrafting.LOGGER.error(e);
        }

    }


    public static class Handler implements IMessageHandler<PacketUpdateAllSharedData, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketUpdateAllSharedData message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    WorldSavedDataCrafters.setData(world, message.craftEndTimes);
                }
            }));

            return null;
        }
    }


}



