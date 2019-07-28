package spatialcrafting.docs;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@SuppressWarnings("ConstantConditions")
public class MyBlockEntityRenderer extends BlockEntityRenderer<DemoBlockEntity> {
    public static void register() {
        BlockEntityRendererRegistry.INSTANCE.register(DemoBlockEntity.class, new MyBlockEntityRenderer());

    }

    private static ItemStack stack = new ItemStack(Items.JUKEBOX, 1);

    @Override
    public void render(DemoBlockEntity blockEntity, double x, double y, double z, float partialTicks, int destroyStage) {

        // Mandatory call before GL calls
        GlStateManager.pushMatrix();

        double offset = Math.sin((blockEntity.getWorld().getTime() + partialTicks) / 8.0) / 4.0;
        // Moves the item around
        GlStateManager.translated(x + 0.5, y + 1.25 + offset, z + 0.5);

        // Rotates the item
        GlStateManager.rotatef((blockEntity.getWorld().getTime() + partialTicks) * 4, 0, 1, 0);


        int light = blockEntity.getWorld().getLightmapIndex(blockEntity.getPos().up(), 0);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (light & 0xFFFF), (float) ((light >> 16) & 0xFFFF));
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Type.GROUND);

        // Mandatory call after GL calls
        GlStateManager.popMatrix();

    }

}
