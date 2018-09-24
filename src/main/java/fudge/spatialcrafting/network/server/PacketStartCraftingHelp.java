package fudge.spatialcrafting.network.server;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.network.PacketBlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class PacketStartCraftingHelp extends PacketBlockPos {

    private int recipeID;

    public PacketStartCraftingHelp() {}

    public PacketStartCraftingHelp(BlockPos masterPos, SpatialRecipe recipe) {
        super(masterPos);
        this.recipeID = recipe.getID();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(recipeID);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        recipeID = buffer.readInt();
    }


    public static class Handler implements IMessageHandler<PacketStartCraftingHelp, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(PacketStartCraftingHelp message, MessageContext ctx) {

            WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();

            serverWorld.addScheduledTask((new Runnable() {
                @Override
                public void run() {
                    try {
                        TileCrafter crafter = CrafterUtil.getClosestMasterBlock(serverWorld, message.pos);
                        if (crafter != null) {
                            crafter.startHelp(SpatialRecipe.fromID(message.recipeID));
                        }
                    }catch (Exception e){
                        SpatialCrafting.LOGGER.error(e);
                    }

                }
            }));

            return null;
        }
    }

}
