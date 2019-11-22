package spatialcrafting.client.particle

import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.client.particle.MathUtil.euclideanDistanceOf
import spatialcrafting.util.Duration
import spatialcrafting.util.ticks

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


    private val randomFloat = this.random.nextFloat() * 3.0f
    private val anotherRandomFloat = this.random.nextFloat() * 3.0f

    init {
        this.maxAge = 500;
        this.setSprite(MinecraftClient.getInstance().itemRenderer.getHeldItemModel(itemStack, world, null).sprite)
        gravityStrength = 1.0f
        scale /= 2.0f
    }


    private var ticksPassed = startTimeDelay


    override fun getType(): ParticleTextureSheet? {
        return ParticleTextureSheet.TERRAIN_SHEET
    }

    override fun getMinU(): Float {
        return sprite.getFrameU(((randomFloat + 1.0f) / 4.0f * 16.0f).toDouble())
    }

    override fun getMaxU(): Float {
        return sprite.getFrameU((randomFloat / 4.0f * 16.0f).toDouble())
    }

    override fun getMinV(): Float {
        return sprite.getFrameV((anotherRandomFloat / 4.0f * 16.0f).toDouble())
    }

    override fun getMaxV(): Float {
        return sprite.getFrameV(((anotherRandomFloat + 1.0f) / 4.0f * 16.0f).toDouble())
    }


    @Override
    //optimize by making calcuations every second, or when a message is sent
    //optimize by making the endPos calculations at a single, seperate place (every tick), and then getting the result on each particle. This is probably not much...
    override fun tick() {
        val endPos: Vec3d = calcEndPos(ticksPassed, craftDuration, Vec3d(endX, origEndY, endZ), currentEndY)
        val pos = Vec3d(x, y, z)

        val direction = endPos.subtract(pos).normalize()

        val speed: Double = calcSpeed(ticksPassed).blocksPerTick

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
