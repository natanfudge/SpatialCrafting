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
                                    val targetLocation: Vec3d
) : Particle(world, itemEntity.x, itemEntity.y, itemEntity.z, 0.0, 0.0, 0.0) {
    //TODO: hide items in hologram until it reaches them
    companion object {

        fun playItemMovementFromPlayerToMultiblock(player: PlayerEntity,
                                                   itemsFromPlayerToMultiblock: List<Pair<BlockPos, ItemStack>>,
                                                   itemsFromMultiblockToPlayer: List<Pair<BlockPos, ItemStack>>) {
            val world = getMinecraftClient().world
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
        return ParticleTextureSheet.CUSTOM
    }

    override fun buildGeometry(bufferBuilder_1: BufferBuilder?, camera_1: Camera?, magicFloat: Float, float_2: Float, float_3: Float, float_4: Float, float_5: Float, float_6: Float) {
        var float_7 = (progress.toFloat() + magicFloat) / maxProgress.toFloat()
        float_7 *= float_7
        val double_1 = itemEntity.x
        val double_2 = itemEntity.y
        val double_3 = itemEntity.z
        val double_4 = targetLocationX(magicFloat)
        val double_5 = targetLocationY(magicFloat)
        val double_6 = targetLocationZ(magicFloat)

        var double_7 = MathHelper.lerp(float_7.toDouble(), double_1, double_4)
        var double_8 = MathHelper.lerp(float_7.toDouble(), double_2, double_5)
        var double_9 = MathHelper.lerp(float_7.toDouble(), double_3, double_6)
        val int_1 = getColorMultiplier(magicFloat)
        val int_2 = int_1 % 65536
        val int_3 = int_1 / 65536
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, int_2.toFloat(), int_3.toFloat())
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        double_7 -= cameraX
        double_8 -= cameraY
        double_9 -= cameraZ
        GlStateManager.enableLighting()
        entityRenderManager!!.render(itemEntity, double_7, double_8, double_9, itemEntity.yaw, magicFloat, false)
    }

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