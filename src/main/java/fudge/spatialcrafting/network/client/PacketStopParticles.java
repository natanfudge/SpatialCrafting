package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.client.util.ParticleUtil;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketBlockPos;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;


// Packet from server to client
@NoArgsConstructor
public class PacketStopParticles extends PacketBlockPos {


    public PacketStopParticles(BlockPos masterPos) {
        super(masterPos);
    }

    public static class Handler implements IMessageHandler<PacketStopParticles, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketStopParticles message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    TileCrafter crafter;
                    try {
                        crafter = Util.getTileEntity(world, message.pos);
                    } catch (NullPointerException e) {
                        // Backup crafter tile entity
                        crafter = Util.getTileEntity(world, message.pos.add(1, 0, 0));
                    }

                    ParticleUtil.stopCraftParticles(crafter);

                }
            }));

            return null;
        }
    }


}



