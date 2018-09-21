package fudge.spatialcrafting.network.server;

import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.network.PacketBlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class PacketStopCraftingHelp extends PacketBlockPos {

    public PacketStopCraftingHelp() {}


    public PacketStopCraftingHelp(BlockPos playerPos) {
        super(playerPos);
    }

    public static class Handler implements IMessageHandler<PacketStopCraftingHelp, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(PacketStopCraftingHelp message, MessageContext ctx) {

            WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();

            serverWorld.addScheduledTask((new Runnable() {
                @Override
                public void run() {
                    TileCrafter crafter = CrafterUtil.getClosestMasterBlock(serverWorld, message.pos);
                    if (crafter != null) {
                        crafter.stopHelp();
                    }

                }
            }));

            return null;
        }
    }

}


