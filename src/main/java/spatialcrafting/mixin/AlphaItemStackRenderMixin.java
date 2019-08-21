import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//package spatialcrafting.mixin;
//
//import net.minecraft.client.color.item.ItemColors;
//import net.minecraft.client.render.BufferBuilder;
//import net.minecraft.client.render.item.ItemRenderer;
//import net.minecraft.client.render.model.BakedModel;
//import net.minecraft.client.render.model.BakedQuad;
//import net.minecraft.item.ItemStack;
//
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.gen.Accessor;
//import org.spongepowered.asm.mixin.gen.Invoker;
//
//@Mixin(ItemRenderer.class)
//public interface AlphaItemStackRenderMixin {
//    @Invoker("renderModel")
//    void renderModelWithColor(BakedModel model, int color, ItemStack stack);
//
//    @Accessor
//    ItemColors getColorMap();
//
//    @Invoker("renderQuad")
//    void invokeRenderQuad(BufferBuilder bufferBuilder_1, BakedQuad bakedQuad_1, int int_1);
//
//
//}
