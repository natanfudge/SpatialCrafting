package fudge.spatialcrafting.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;

public final class RenderUtil {

    private RenderUtil() {}

    // Copied from RenderItem::renderItem to allow for different alpha
    public static void renderGhostItem(ItemStack stack, IBakedModel model) {
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            if (model.isBuiltInRenderer()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
            } else {
                itemRenderer.renderModel(model, 0x7f_ff_ff_ff, stack);

                if (stack.hasEffect()) {
                    itemRenderer.renderEffect(model);
                }
            }

            GlStateManager.popMatrix();
        }
    }


}
