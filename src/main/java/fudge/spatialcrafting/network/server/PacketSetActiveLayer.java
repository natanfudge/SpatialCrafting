package fudge.spatialcrafting.network.server;

import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.network.PacketBlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


// Packet from server to client
public class PacketSetActiveLayer extends PacketBlockPos {

    private int layerToSet;

    public PacketSetActiveLayer(BlockPos playerPos, int layer) {
        super(playerPos);
        this.layerToSet = layer;
    }

    // Necessary for reflection
    public PacketSetActiveLayer() {
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(layerToSet);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        layerToSet = buffer.readInt();
    }


    public static class Handler implements IMessageHandler<PacketSetActiveLayer, IMessage> {

        @Override
        public IMessage onMessage(PacketSetActiveLayer message, MessageContext ctx) {

            WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();

            serverWorld.addScheduledTask((new Runnable() {
                @Override
                public void run() {
                    TileCrafter crafter = CrafterUtil.getClosestMasterBlock(serverWorld, message.pos);
                    if (crafter != null) {
                        if (crafter.size() > message.layerToSet) {
                            if (!crafter.isCrafting()) {
                                crafter.setActiveHolograms(message.layerToSet);
                            }
                        }
                    }


                }
            }));

            return null;
        }
    }


}



