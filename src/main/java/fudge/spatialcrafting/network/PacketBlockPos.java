package fudge.spatialcrafting.network;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;


// Packet from server to client
@NoArgsConstructor
public class PacketBlockPos implements IMessage {

    protected BlockPos pos;

    public PacketBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        pos = BlockPos.fromLong(buffer.readLong());
    }


}