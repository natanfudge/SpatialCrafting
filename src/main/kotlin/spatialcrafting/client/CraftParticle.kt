package spatialcrafting.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.client.MathUtil.euclideanDistanceOf

class CraftParticle(world: World,
                    sourcePos: Vec3d,
                    val endX: Double,
                    val origEndY: Double,
                    val endZ: Double,
                    startTimeDelay: Duration,
                    val craftDuration: Duration,
                    val currentEndY: Double,
                    itemStack: ItemStack
) : SpriteBillboardParticle(world, sourcePos.x, sourcePos.y, sourcePos.z, 0.0, 0.0, 0.0) {


    //TODO: submit yarn mappings for these
    private val field_17783 = this.random.nextFloat() * 3.0f
    private val field_17784 = this.random.nextFloat() * 3.0f

    init {
        this.maxAge = 500;
        this.setSprite(MinecraftClient.getInstance().itemRenderer.getHeldItemModel(itemStack, world, null).sprite)
        gravityStrength = 1.0f
        scale /= 2.0f
    }

    companion object {
        private const val TICKS_PER_SECOND = 20
//TODO: clean this up (duration wrapping)
        const val PHASE_2_Y_END_POS_INCREASE_PER_TICK: Float = 0.5f / TICKS_PER_SECOND
        const val SPEED_BLOCKS_PER_TICK_BASE: Float = 1.0f / TICKS_PER_SECOND
        const val SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK: Double = 0.2f / TICKS_PER_SECOND / TICKS_PER_SECOND.toDouble()
        val PHASE_2_START = 5.seconds
        const val SLAMDOWN_SPEED_BLOCKS_PER_TICK = 4f / 20

    }

    private var ticksPassed = startTimeDelay


    override fun getType(): ParticleTextureSheet? {
        return ParticleTextureSheet.TERRAIN_SHEET
    }

    override fun getMinU(): Float {
        return sprite.getU(((field_17783 + 1.0f) / 4.0f * 16.0f).toDouble())
    }

    override fun getMaxU(): Float {
        return sprite.getU((field_17783 / 4.0f * 16.0f).toDouble())
    }

    override fun getMinV(): Float {
        return sprite.getV((field_17784 / 4.0f * 16.0f).toDouble())
    }

    override fun getMaxV(): Float {
        return sprite.getV(((field_17784 + 1.0f) / 4.0f * 16.0f).toDouble())
    }


    @Override
    //optimize by making calcuations every second, or when a message is sent
    //optimize by making the endPos calculations at a single, seperate place (every tick), and then getting the result on each particle. This is probably not much...
    override fun tick() {


        val endPos: Vec3d = ParticleUtil.calcEndPos(ticksPassed, craftDuration, Vec3d(endX, origEndY, endZ), currentEndY)
        val pos = Vec3d(x, y, z)

        val direction = endPos.subtract(pos).normalize()

        val speed: Double = ParticleUtil.calcSpeed(ticksPassed)

        velocityX = direction.x * speed
        velocityY = direction.y * speed
        velocityZ = direction.z * speed


        this.prevPosX = this.x
        this.prevPosY = this.y
        this.prevPosZ = this.z


        if (euclideanDistanceOf(endPos, pos) < speed) {
            this.markDead()
        }

        this.ticksPassed += 1.ticks
        if (this.ticksPassed >= 1000.ticks) {
            this.markDead()
        }


        this.move(this.velocityX, this.velocityY, this.velocityZ)


    }


}
