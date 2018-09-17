package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.debug.Debug;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;


// Packet from server to client
@NoArgsConstructor
public class PacketAddRecipeToJei implements IMessage {

    SpatialRecipe recipe;

    public PacketAddRecipeToJei(SpatialRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        recipe = SpatialRecipe.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        recipe.toBytes(buf);
    }


    public static class Handler implements IMessageHandler<PacketAddRecipeToJei, IMessage> {

        @Override
        @Nullable
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketAddRecipeToJei message, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask((new Runnable() {
                @Override
                @SideOnly(Side.CLIENT)
                public void run() {
                    //TODO this is where it gets interesting..
                    System.out.println(message.recipe);
                }
            }));

            return null;
        }
    }


}



