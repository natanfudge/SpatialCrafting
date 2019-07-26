package spatialcrafting.hologram

import com.mojang.blaze3d.platform.GlStateManager
import com.sun.prism.TextureMap
import com.mojang.blaze3d.platform.GlStateManager.bindTexture
import org.lwjgl.opengl.GL11
import net.minecraft.block.ChestBlock.getInventory
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.item.ItemStack
import kotlin.math.sin


class HologramBlockEntityRenderer : BlockEntityRenderer<HologramBlockEntity>() {

    //TODO: Copy fastTESR from someone
    override fun render(tile: HologramBlockEntity, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        val stack = tile.getItem()
        if (!stack.isEmpty()) {
            val minecraft = MinecraftClient.getInstance()


            // Must be done for all GL calls
            GlStateManager.pushMatrix()
            // Required for the item to render properly
            GlStateManager.enableRescaleNormal()
            GlStateManager.alphaFunc(GL11.GL_GREATER, ALPHA_FUNC_REF)
            GlStateManager.enableBlend()
//TODO            RenderHelper.enableStandardItemLighting()
            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)


            val time = tile.world!!.time + partialTicks

            // Changes the position of the item to float up and down like a sine wave.
            val offset = sin((time - tile.lastChangeTime) * OFFSET_CHANGE_SPEED_MULTIPLIER) * OFFSET_AMOUNT_MULTIPLIER
            GlStateManager.translated(x + MOVE_TO_MID_BLOCK_OFFSET, y + offset + HEIGHT_INCREASE, z + MOVE_TO_MID_BLOCK_OFFSET)

            // Makes the item bigger
            GlStateManager.scalef(SIZE_MULTIPLIER.toFloat(), SIZE_MULTIPLIER.toFloat(), SIZE_MULTIPLIER.toFloat())

            // Spins the item around
            GlStateManager.rotatef(time, 0f, SPIN_SPEED.toFloat(), 0.toFloat())

            // Gets model
            var model = minecraft.itemRenderer.getModel(stack, tile.world, null)
// TODO           model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false)

            // Attaches texture and renders model
//            minecraft.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
//            if (tile.isDisplayingGhostItem()) {
//                RenderUtil.renderGhostItem(stack, model)
//            }
//            else {
            minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND)
//            }

            // Enabled GL stuff must be disabled after.
            GlStateManager.scalef(1f, 1f, 1f)
            GlStateManager.popMatrix()
            GlStateManager.disableRescaleNormal()
            GlStateManager.disableBlend()

        }
    }

    companion object {

        private val ALPHA_FUNC_REF = 0.1f
        private val OFFSET_AMOUNT_MULTIPLIER = 0.05
        private val OFFSET_CHANGE_SPEED_MULTIPLIER = 0.125
        private val MOVE_TO_MID_BLOCK_OFFSET = 0.5
        private val HEIGHT_INCREASE = 0.3
        private val SIZE_MULTIPLIER = 2
        private val SPIN_SPEED = 1
    }


}
