package spatialcrafting.client.particle

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider.Immediate
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.getHologramEntity
import spatialcrafting.util.dropItemStack
import spatialcrafting.util.getMinecraftClient
import spatialcrafting.util.plus

private val HalfOfEachCoordinate = Vec3d(0.5, 0.5, 0.5)

@Environment(EnvType.CLIENT)
abstract class ItemMovementParticle(world: World,
                                    private val itemEntity: ItemEntity,
                                    private val targetLocation: Vec3d
) : Particle(world, itemEntity.x, itemEntity.y, itemEntity.z, 0.0, 0.0, 0.0) {
    private val bufferBuilderStorage  = getMinecraftClient().bufferBuilders
    private val entityRenderDispatcher = getMinecraftClient().entityRenderManager
    companion object {

        fun playItemMovementFromPlayerToMultiblock(player: PlayerEntity,
                                                   itemsFromPlayerToMultiblock: List<Pair<BlockPos, ItemStack>>,
                                                   itemsFromMultiblockToPlayer: List<Pair<BlockPos, ItemStack>>) {

            val world = getMinecraftClient().world!!
            val particleManager = getMinecraftClient().particleManager

            for ((pos, stack) in itemsFromMultiblockToPlayer) {

                val entity = world.dropItemStack(stack, pos + HalfOfEachCoordinate)
                particleManager.addParticle(ItemMovementParticleToPlayer(world, entity, player))
            }
            for ((pos, stack) in itemsFromPlayerToMultiblock) {
                val hologramEntity = world.getHologramEntity(pos)
                hologramEntity.contentsAreTravelling = true
                val entity = world.dropItemStack(stack, player.pos)
                particleManager.addParticle(ItemMovementParticleToHologram(world, entity, hologramEntity))
            }
        }
    }

    open fun targetLocationX(magicFloat: Float) = targetLocation.x
    open fun targetLocationY(magicFloat: Float) = targetLocation.y
    open fun targetLocationZ(magicFloat: Float) = targetLocation.z

    private var progress = 0
    private val maxProgress = 3
    private val entityRenderManager = getMinecraftClient().entityRenderManager

    override fun getType(): ParticleTextureSheet? {
        itemEntity.x
        return ParticleTextureSheet.CUSTOM
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        var f: Float = (progress.toFloat() + tickDelta) / 3.0f
        f *= f
        val h = MathHelper.lerp(f.toDouble(), itemEntity.x, targetLocationX(tickDelta))
        val i = MathHelper.lerp(f.toDouble(), itemEntity.y, targetLocationY(tickDelta))
        val j = MathHelper.lerp(f.toDouble(), itemEntity.z, targetLocationZ(tickDelta))
        val immediate = bufferBuilderStorage.entityVertexConsumers
        val vec3d: Vec3d = camera.pos
        entityRenderManager.render(itemEntity, h - vec3d.getX(), i - vec3d.getY(),
                j - vec3d.getZ(), itemEntity.yaw, tickDelta, MatrixStack(), immediate,
                entityRenderDispatcher.method_23839(itemEntity, tickDelta))
        immediate.draw()
    }

//    override fun buildGeometry(bufferBuilder_1: BufferBuilder?, camera_1: Camera?, magicFloat: Float, float_2: Float, float_3: Float, float_4: Float, float_5: Float, float_6: Float) {
//
//
//        var float7 = (progress.toFloat() + magicFloat) / maxProgress.toFloat()
//        float7 *= float7
//        val double1 = itemEntity.x
//        val double2 = itemEntity.y
//        val double3 = itemEntity.z
//        val double4 = targetLocationX(magicFloat)
//        val double5 = targetLocationY(magicFloat)
//        val double6 = targetLocationZ(magicFloat)
//
//        var double7 = MathHelper.lerp(float7.toDouble(), double1, double4)
//        var double8 = MathHelper.lerp(float7.toDouble(), double2, double5)
//        var double9 = MathHelper.lerp(float7.toDouble(), double3, double6)
//        val int1 = getColorMultiplier(magicFloat)
//        val int2 = int1 % 65536
//        val int3 = int1 / 65536
//        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, int2.toFloat(), int3.toFloat())
//        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
//        double7 -= cameraX
//        double8 -= cameraY
//        double9 -= cameraZ
//        GlStateManager.enableLighting()
//        entityRenderManager!!.render(itemEntity, double7, double8, double9, itemEntity.yaw, magicFloat, false)
//    }

    override fun tick() {
        ++progress
        if (progress == maxProgress) {
            markDead()
        }
    }

}

class ItemMovementParticleToHologram(world: World, itemEntity: ItemEntity, private val hologram: HologramBlockEntity)
    : ItemMovementParticle(world, itemEntity, hologram.pos + HalfOfEachCoordinate) {
    override fun markDead() {
        super.markDead()
        hologram.contentsAreTravelling = false
    }
}

class ItemMovementParticleToPlayer(world: World,
                                   itemEntity: ItemEntity,
                                   private val targetPlayer: PlayerEntity
) : ItemMovementParticle(world, itemEntity, targetPlayer.pos) {
    override fun targetLocationX(magicFloat: Float) = MathHelper.lerp(magicFloat.toDouble(), targetPlayer.prevRenderX, targetPlayer.x)
    override fun targetLocationY(magicFloat: Float) = MathHelper.lerp(magicFloat.toDouble(), targetPlayer.prevRenderY, targetPlayer.y)
    override fun targetLocationZ(magicFloat: Float) = MathHelper.lerp(magicFloat.toDouble(), targetPlayer.prevRenderZ, targetPlayer.z)
}