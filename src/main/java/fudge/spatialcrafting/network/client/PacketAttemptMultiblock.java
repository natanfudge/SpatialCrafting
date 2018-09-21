package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.network.PacketBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;


// Packet from server to client
public class PacketAttemptMultiblock extends PacketBlockPos {

    public PacketAttemptMultiblock() {}

    public PacketAttemptMultiblock(BlockPos crafterPos) {
        super(crafterPos);
    }

    public static class Handler implements IMessageHandler<PacketAttemptMultiblock, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketAttemptMultiblock message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;
                    int crafterSize = ((BlockCrafter) world.getBlockState(message.pos).getBlock()).size();
                    EntityLivingBase player = Minecraft.getMinecraft().player;

                    CrafterUtil.attemptMultiblock(world, message.pos, player, crafterSize);

                }
            }));

            return null;
        }
    }


}



