package fudge.spatialcrafting.client.tile;

import fudge.spatialcrafting.common.tile.TileHologram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

public class TESRHologram extends TileEntitySpecialRenderer<TileHologram> {

    private static final float ALPHA_FUNC_REF = 0.1f;
    private static final double OFFSET_AMOUNT_MULTIPLIER = 0.05;
    private static final double OFFSET_CHANGE_SPEED_MULTIPLIER = 0.125;
    private static final double MOVE_TO_MID_BLOCK_OFFSET = 0.5;
    private static final double HEIGHT_INCREASE = 0.3;
    private static final int SIZE_MULTIPLIER = 2;
    private static final int SPIN_SPEED = 1;

    //TODO: Copy fastTESR from someone
    @Override
    public void render(TileHologram tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = tile.getInventory().getStackInSlot(0);
        if (!stack.isEmpty()) {
            final Minecraft minecraft = Minecraft.getMinecraft();

            // Must be done for all GL calls
            GlStateManager.pushMatrix();
            // Required for the item to render properly
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(GL11.GL_GREATER, ALPHA_FUNC_REF);
            GlStateManager.enableBlend();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);


            double time = tile.getWorld().getTotalWorldTime() + partialTicks;

            // Changes the position of the item to float up and down like a sine wave.
            double offset = Math.sin((time - tile.getLastChangeTime()) * OFFSET_CHANGE_SPEED_MULTIPLIER) * OFFSET_AMOUNT_MULTIPLIER;
            GlStateManager.translate(x + MOVE_TO_MID_BLOCK_OFFSET, y + offset + HEIGHT_INCREASE, z + MOVE_TO_MID_BLOCK_OFFSET);

            // Makes the item bigger
            GlStateManager.scale(SIZE_MULTIPLIER, SIZE_MULTIPLIER, SIZE_MULTIPLIER);

            // Spins the item around
            GlStateManager.rotate((float) time, 0, SPIN_SPEED, 0);
            GlStateManager.color(1, 1, 1, 0.5f);

            // Gets model
            IBakedModel model = minecraft.getRenderItem().getItemModelWithOverrides(stack, tile.getWorld(), null);
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);

            // Attaches texture and renders model
            minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            minecraft.getRenderItem().renderItem(stack, model);

            // Enabled GL stuff must be disabled after.
            GlStateManager.scale(1, 1, 1);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();

        }
    }
}


