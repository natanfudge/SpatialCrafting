package spatialcrafting.client.particle

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.util.getMinecraftClient

@Environment(EnvType.CLIENT)
class MyItemPickupParticle (world: World,
                          private val itemEntity: ItemEntity,
                          private val targetLocation: Vec3d
//                          private val yModifier: Float,
//                          vec3d: Vec3d
)
    : Particle(world, itemEntity.x, itemEntity.y, itemEntity.z, 0.0,0.0,0.0) {
    private var progress = 0
    private val maxProgress = 3
    private val entityRenderManager = getMinecraftClient().entityRenderManager

    override fun getType(): ParticleTextureSheet? {
        return ParticleTextureSheet.CUSTOM
    }

    override fun buildGeometry(bufferBuilder_1: BufferBuilder?, camera_1: Camera?, float_1: Float, float_2: Float, float_3: Float, float_4: Float, float_5: Float, float_6: Float) {
        var float_7 = (progress.toFloat() + float_1) / maxProgress.toFloat()
        float_7 *= float_7
        val double_1 = itemEntity.x
        val double_2 = itemEntity.y
        val double_3 = itemEntity.z
//        val double_4 = MathHelper.lerp(float_1.toDouble(), targetEntity.prevRenderX, targetEntity.x)
//        val double_5 = MathHelper.lerp(float_1.toDouble(), targetEntity.prevRenderY, targetEntity.y) + yModifier.toDouble()
//        val double_6 = MathHelper.lerp(float_1.toDouble(), targetEntity.prevRenderZ, targetEntity.z)
        val double_4 = targetLocation.x
        val double_5 = targetLocation.y
        val double_6 = targetLocation.z

        var double_7 = MathHelper.lerp(float_7.toDouble(), double_1, double_4)
        var double_8 = MathHelper.lerp(float_7.toDouble(), double_2, double_5)
        var double_9 = MathHelper.lerp(float_7.toDouble(), double_3, double_6)
        val int_1 = getColorMultiplier(float_1)
        val int_2 = int_1 % 65536
        val int_3 = int_1 / 65536
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, int_2.toFloat(), int_3.toFloat())
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        double_7 -= cameraX
        double_8 -= cameraY
        double_9 -= cameraZ
        GlStateManager.enableLighting()
        entityRenderManager!!.render(itemEntity, double_7, double_8, double_9, itemEntity.yaw, float_1, false)
    }

    override fun tick() {
        ++progress
        if (progress == maxProgress) {
            markDead()
        }
    }

}