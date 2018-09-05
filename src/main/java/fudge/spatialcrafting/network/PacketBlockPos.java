package fudge.spatialcrafting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;


// Packet from server to client
public class PacketBlockPos implements IMessage {

    protected BlockPos pos;

    public PacketBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    // Necessary for reflection
    public PacketBlockPos() {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        pos = BlockPos.fromLong(buffer.readLong());
    }



    /*
    package fudge.spatialcrafting.network;

import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.network.client.PacketRemoveMasterBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


// Packet from server to client
public class PacketBlockPos implements IMessage {

    protected BlockPos pos;

    public PacketBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    // Necessary for reflection
    public PacketBlockPos() {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        pos = BlockPos.fromLong(buffer.readLong());
    }


    public static class Handler implements IMessageHandler<PacketRemoveMasterBlock, IMessage> {

        private static BlockPosConsumer consumer;

        public static void setBlockPosConsumer(BlockPosConsumer consumer){
            Handler.consumer = consumer;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketRemoveMasterBlock message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask(()->consumer.useBlockPos(message.pos));

            return null;
        }
    }

    private interface BlockPosConsumer{
        void useBlockPos(BlockPos pos);
    }

}




     */
}



