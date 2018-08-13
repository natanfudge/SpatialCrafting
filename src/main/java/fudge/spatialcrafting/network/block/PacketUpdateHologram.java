package fudge.spatialcrafting.network.block;

import fudge.spatialcrafting.common.tile.TileHologram;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


// Packet from server to client
public class PacketUpdateHologram implements IMessage {


    private BlockPos messagePos;
    private ItemStack stackMessage;
    private long lastChangeTimeMessage;


    public PacketUpdateHologram(BlockPos pos, ItemStack stack, long lastChangeTime) {

        this.messagePos = pos;
        this.stackMessage = stack;
        this.lastChangeTimeMessage = lastChangeTime;

    }

    public PacketUpdateHologram(TileHologram tile) {
        this(tile.getPos(), tile.getInventory().getStackInSlot(0), tile.getLastChangeTime());
    }

    // Necessary for reflection
    public PacketUpdateHologram() {
    }

    @Override
    public void toBytes(ByteBuf buf) {

        // Must be done in the order of the constructor
        buf.writeLong(messagePos.toLong());
        ByteBufUtils.writeItemStack(buf, stackMessage);
        buf.writeLong(lastChangeTimeMessage);

    }

    @Override
    public void fromBytes(ByteBuf buf) {

        // Must be done in the order of the constructor
        messagePos = BlockPos.fromLong(buf.readLong());
        stackMessage = ByteBufUtils.readItemStack(buf);
        lastChangeTimeMessage = buf.readLong();

    }


    public static class Handler implements IMessageHandler<PacketUpdateHologram, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketUpdateHologram message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    if (world.isBlockLoaded(message.messagePos)) {
                        TileHologram tile = (TileHologram) world.getTileEntity(message.messagePos);
                        tile.getInventory().setStackInSlot(0, message.stackMessage);
                        tile.setLastChangeTime(message.lastChangeTimeMessage);
                    }
                }
            }));

            return null;
        }
    }


}


