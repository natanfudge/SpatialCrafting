package spatialcrafting.client.particle

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
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
    private val bufferBuilderStorage = getMinecraftClient().bufferBuilders
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