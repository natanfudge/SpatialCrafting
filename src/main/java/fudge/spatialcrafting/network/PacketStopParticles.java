package fudge.spatialcrafting.network;

import fudge.spatialcrafting.client.particle.ParticleItemDust;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.Util;
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
public class PacketStopParticles implements IMessage {

    private BlockPos masterPos;

    public PacketStopParticles(BlockPos pos) {
        masterPos = pos;
    }

    // Necessary for reflection
    public PacketStopParticles() {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(masterPos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        masterPos = BlockPos.fromLong(buffer.readLong());
    }


    public static class Handler implements IMessageHandler<PacketStopParticles, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketStopParticles message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    TileCrafter crafter = Util.getTileEntity(world, message.masterPos);

                    // Backup crafter tile entity
                    if (crafter == null) {
                        crafter = Util.getTileEntity(world, message.masterPos.add(1, 0, 0));
                    }


                    ParticleItemDust.stopParticles(crafter);

                }
            }));

            return null;
        }
    }


}



