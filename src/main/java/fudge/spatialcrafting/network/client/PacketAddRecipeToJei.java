package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.compat.jei.ScJeiPlugin;
import fudge.spatialcrafting.compat.jei.WrapperSpatialRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;


// Packet from server to client
public class PacketAddRecipeToJei implements IMessage {

    private SpatialRecipe recipe;

    public PacketAddRecipeToJei() {}

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
                    String categoryUID = SpatialCrafting.MODID + message.recipe.size();
                    //noinspection deprecation
                    ScJeiPlugin.RECIPE_REGISTRY.addRecipe(new WrapperSpatialRecipe(message.recipe), categoryUID);
                }
            }));

            return null;
        }
    }


}



