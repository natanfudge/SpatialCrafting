package spatialcrafting.hologram

//import net.minecraft.block.RenderLayer
import fabricktx.api.getMinecraftClient
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import spatialcrafting.client.keybinding.MinimizeHologramsKeyBinding
import spatialcrafting.modId
import java.util.*
import java.util.function.Supplier


class HologramBakedModel : FabricBakedModel, BakedModel {
    companion object {
        val Texture = modId("block/hologram")
        private const val Alpha = 0x7F
        private const val AlphaMask = (Alpha shl (6 * 4))
        private const val FullRGB = 0xFFFFFF
        private val quadTransform: RenderContext.QuadTransform = RenderContext.QuadTransform { quad ->
            quad.material(RendererAccess.INSTANCE.renderer.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find())
            // Get the color of the stored stack in all 4 vertices
            val c0 = quad.spriteColor(0, 0)
            val c1 = quad.spriteColor(1, 0)
            val c2 = quad.spriteColor(2, 0)
            val c3 = quad.spriteColor(3, 0)

            // Make all of these colors transparent and apply them

            val transparentC0 = (c0 and AlphaMask) or (c0 and FullRGB)
            val transparentC1 = (c1 and AlphaMask) or (c1 and FullRGB)
            val transparentC2 = (c2 and AlphaMask) or (c2 and FullRGB)
            val transparentC3 = (c3 and AlphaMask) or (c3 and FullRGB)

            quad.spriteColor(0, transparentC0, transparentC1, transparentC2, transparentC3)

            val scale = 0.5

            // Rescales the model
            for (i in 0..3) {
                quad.pos(i, ((quad.x(i) - 0.5f) * scale + 0.5f).toFloat(),
                        ((quad.y(i) - 0.5f) * scale + 0.5f).toFloat(),
                        ((quad.z(i) - 0.5f) * scale + 0.5f).toFloat())
            }
            true
        }

        private fun hologramCubeMesh(left: Float, bottom: Float, right: Float, top: Float, depth : Float): Mesh {
            val baseColor = 0xFF_FF_FF_FF.toInt()
            val renderer = RendererAccess.INSTANCE.renderer
            val mb = renderer.meshBuilder()
            val qe = mb.emitter
            val mat = renderer.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find()
            val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            val spriteBase = atlas.apply(Texture)

            fun emitSquare(side: Direction) {
                qe.material(mat).square(side, left, bottom, right, top, depth)
                        .spriteBake(0, spriteBase, MutableQuadView.BAKE_LOCK_UV or MutableQuadView.BAKE_NORMALIZED)
                        .spriteColor(0, baseColor, baseColor, baseColor, baseColor).emit()
            }

            emitSquare(Direction.UP)
            emitSquare(Direction.DOWN)
            emitSquare(Direction.EAST)
            emitSquare(Direction.WEST)
            emitSquare(Direction.NORTH)
            emitSquare(Direction.SOUTH)

            return mb.build()
        }


        val fullHologramMesh: Mesh by lazy { hologramCubeMesh(0f, 0f, 1f, 1f,0f) }
        val halfHologramMesh: Mesh by lazy { hologramCubeMesh(0.25f, 0.25f, 0.75f, 0.75f,0.25f) }

    }


    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
    }

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos,
                                randomSupplier: Supplier<Random>, context: RenderContext) {
        val minecraft = getMinecraftClient()

        context.meshConsumer().accept(
                if (MinimizeHologramsKeyBinding.isPressed) halfHologramMesh
                else fullHologramMesh
        )

        blockView as RenderAttachedBlockView
        val stack = blockView.getBlockEntityRenderAttachment(pos) as ItemStack? ?: return
        if (!stack.isEmpty) {
            context.pushTransform(quadTransform)
            context.fallbackConsumer().accept(minecraft.itemRenderer.models.getModel(stack))
            context.popTransform()
        }


    }

    override fun isVanillaAdapter(): Boolean = false
    override fun getQuads(var1: BlockState?, var2: Direction?, var3: Random?): List<BakedQuad> = listOf()
    // Not actually used
    override fun getSprite(): Sprite = getMinecraftClient().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Texture)

    override fun useAmbientOcclusion(): Boolean = true
    override fun hasDepth(): Boolean = false
    override fun getTransformation(): ModelTransformation = ModelHelper.MODEL_TRANSFORM_BLOCK
    override fun isSideLit(): Boolean = false


    override fun isBuiltin(): Boolean = false
    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

}