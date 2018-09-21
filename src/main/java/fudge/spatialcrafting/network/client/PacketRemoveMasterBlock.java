package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.network.PacketBlockPos;
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
public class PacketRemoveMasterBlock extends PacketBlockPos {

    public PacketRemoveMasterBlock() {}

    public PacketRemoveMasterBlock(BlockPos masterPos) {
        super(masterPos);
    }

    public static class Handler implements IMessageHandler<PacketRemoveMasterBlock, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketRemoveMasterBlock message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;

                    WorldSavedDataCrafters.removeData(world, message.pos, false);

                }
            }));

            return null;
        }
    }


}



