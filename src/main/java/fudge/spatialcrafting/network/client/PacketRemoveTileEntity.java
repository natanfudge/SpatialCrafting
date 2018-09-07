package fudge.spatialcrafting.network.client;

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

@NoArgsConstructor
public class PacketRemoveTileEntity extends PacketBlockPos {

    public PacketRemoveTileEntity(BlockPos tilePos) {
        super(tilePos);
    }

    public static class Handler implements IMessageHandler<PacketRemoveTileEntity, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketRemoveTileEntity message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    World world = Minecraft.getMinecraft().world;

                    world.removeTileEntity(message.pos);

                }
            }));

            return null;
        }
    }

}
