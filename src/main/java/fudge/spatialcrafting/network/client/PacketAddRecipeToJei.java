package fudge.spatialcrafting.network.client;

import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.util.RecipeUtil;
import fudge.spatialcrafting.compat.jei.ScJeiPlugin;
import fudge.spatialcrafting.compat.jei.WrapperSpatialRecipe;
import io.netty.buffer.ByteBuf;
import mezz.jei.JustEnoughItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;


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
                    ScJeiPlugin.RECIPE_REGISTRY.addRecipe(new WrapperSpatialRecipe(message.recipe),categoryUID);
                }
            }));

            return null;
        }
    }


    /*
    [12:16:34] [main/FATAL]: Error executing task
java.util.concurrent.ExecutionException: java.lang.NullPointerException
	at java.util.concurrent.FutureTask.report(FutureTask.java:122) ~[?:1.8.0_51]
	at java.util.concurrent.FutureTask.get(FutureTask.java:192) ~[?:1.8.0_51]
	at net.minecraft.util.Util.func_181617_a(SourceFile:47) [h.class:?]
	at net.minecraft.client.Minecraft.func_71411_J(Minecraft.java:1086) [bib.class:?]
	at net.minecraft.client.Minecraft.func_99999_d(Minecraft.java:397) [bib.class:?]
	at net.minecraft.client.main.Main.main(SourceFile:123) [Main.class:?]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:1.8.0_51]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:1.8.0_51]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:1.8.0_51]
	at java.lang.reflect.Method.invoke(Method.java:497) ~[?:1.8.0_51]
	at net.minecraft.launchwrapper.Launch.launch(Launch.java:135) [launchwrapper-1.12.jar:?]
	at net.minecraft.launchwrapper.Launch.main(Launch.java:28) [launchwrapper-1.12.jar:?]
Caused by: java.lang.NullPointerException
	at fudge.spatialcrafting.network.client.PacketAddRecipeToJei$Handler$1.run(PacketAddRecipeToJei.java:58) ~[PacketAddRecipeToJei$Handler$1.class:?]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) ~[?:1.8.0_51]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) ~[?:1.8.0_51]
	at net.minecraft.util.Util.func_181617_a(SourceFile:46) ~[h.class:?]
	... 9 more
     */


}



