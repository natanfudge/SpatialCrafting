//package spatialcrafting.client
//
//import net.fabricmc.api.EnvType
//import net.fabricmc.api.Environment
//import net.fabricmc.fabric.api.server.PlayerStream.world
//import net.minecraft.client.particle.CrackParticle
//import net.minecraft.client.particle.Particle
//import net.minecraft.client.particle.SpriteBillboardParticle
//import net.minecraft.client.render.BufferBuilder
//import net.minecraft.client.texture.Sprite
//import net.minecraft.client.texture.SpriteAtlasTexture
//import net.minecraft.entity.Entity
//import net.minecraft.item.ItemStack
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Vec3d
//import net.minecraft.world.World
//
//
////TODO particle improvement : when they reach the top, the particle stop appearing and they start circling that spot. Then after a bit they fall down quickly and explode to the sides.
//@Environment(EnvType.CLIENT)
//class ParticleCraft(
//        worldIn: World?,
//        startPos: Vec3d,
//        origEndPos: Vec3d,
//        private var ticksPassed: Int,
//        private val craftDuration: Int,
//        private val craftYEndPos: Double,
//        itemStack: ItemStack)
//    : CrackParticle(worldIn, startPos.x, startPos.y, startPos.z, 0.0 , 0.0, 0.0, itemStack) {
//
////    private val sourcePos: Vec3d
////    private val endZ: Double
////    private val endX: Double
////    private val origEndY: Double
////    val fXLayer: Int
////        get() = 1
////
////    //optimize by making calcuations every second, or when a message is sent
////    //optimize by making the endPos calculations at a single, seperate place (every tick), and then getting the result on each particle. This is probably not much...
////    fun onUpdate() {
////        val endPos: Vec3d = ParticleUtil.calcEndPos(ticksPassed, craftDuration, Vec3d(endX, origEndY, endZ), craftYEndPos)
////        val pos = Vec3d(posX, posY, posZ)
////        val direction: Vec3d = endPos.subtract(pos).normalize()
////        val speed: Double = ParticleUtil.calcSpeed(ticksPassed)
////        motionX = direction.x * speed
////        motionY = direction.y * speed
////        motionZ = direction.z * speed
////        this.prevPosX = this.posX
////        this.prevPosY = this.posY
////        this.prevPosZ = this.posZ
////        if (euclideanDistanceOf(endPos, pos) < /* 0.1 + */speed) {
////            this.setExpired()
////        }
////        if (ticksPassed++ >= 1000) {
////            this.setExpired()
////        }
////        this.move(this.motionX, this.motionY, this.motionZ)
////    }
////
////    fun renderParticle(buffer: BufferBuilder, entityIn: Entity?, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float) {
////        var f: Float = (this.x as Float + this.particleTextureJitterX / 4.0f) / 16.0f
////        var f1 = f + 0.015609375f
////        var f2: Float = (this.particleTextureIndexY as Float + this.particleTextureJitterY / 4.0f) / 16.0f
////        var f3 = f2 + 0.015609375f
////        val f4: Float = 0.1f * this.particleScale
////        if (this.particleTexture != null) {
////            f = this.particleTexture.getInterpolatedU((this.particleTextureJitterX / 4.0f * 16.0f) as Double)
////            f1 = this.particleTexture.getInterpolatedU(((this.particleTextureJitterX + 1.0f) / 4.0f * 16.0f) as Double)
////            f2 = this.particleTexture.getInterpolatedV((this.particleTextureJitterY / 4.0f * 16.0f) as Double)
////            f3 = this.particleTexture.getInterpolatedV(((this.particleTextureJitterY + 1.0f) / 4.0f * 16.0f) as Double)
////        }
////        val f5 = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks.toDouble() - interpPosX) as Float
////        val f6 = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks.toDouble() - interpPosY) as Float
////        val f7 = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks.toDouble() - interpPosZ) as Float
////        val i = getBrightnessForRender(partialTicks)
////        val j = i shr 16 and '\uffff'.toInt()
////        val k = i and '\uffff'.toInt()
////        buffer.pos((f5 - rotationX * f4 - rotationXY * f4).toDouble(),
////                (f6 - rotationZ * f4).toDouble(),
////                (f7 - rotationYZ * f4 - rotationXZ * f4).toDouble()).tex(f.toDouble(), f3.toDouble()).color(this.particleRed,
////                this.colorGreen,
////                this.colorBlue,
////                1.0f).lightmap(j, k).endVertex()
////        buffer.pos((f5 - rotationX * f4 + rotationXY * f4).toDouble(),
////                (f6 + rotationZ * f4).toDouble(),
////                (f7 - rotationYZ * f4 + rotationXZ * f4).toDouble()).tex(f.toDouble(), f2.toDouble()).color(this.particleRed,
////                this.colorGreen,
////                this.colorBlue,
////                1.0f).lightmap(j, k).endVertex()
////        buffer.pos((f5 + rotationX * f4 + rotationXY * f4).toDouble(),
////                (f6 + rotationZ * f4).toDouble(),
////                (f7 + rotationYZ * f4 + rotationXZ * f4).toDouble()).tex(f1.toDouble(), f2.toDouble()).color(this.particleRed,
////                this.colorGreen,
////                this.colorBlue,
////                1.0f).lightmap(j, k).endVertex()
////        buffer.pos((f5 + rotationX * f4 - rotationXY * f4).toDouble(),
////                (f6 - rotationZ * f4).toDouble(),
////                (f7 + rotationYZ * f4 - rotationXZ * f4).toDouble()).tex(f1.toDouble(), f3.toDouble()).color(this.particleRed,
////                this.colorGreen,
////                this.colorBlue,
////                1.0f).lightmap(j, k).endVertex()
////    }
////
////    override fun method_3087(float_1: Float): Particle {
////        return super.method_3087(float_1)
////    }
////
//////    fun getBrightnessForRender(partialTick: Float): Int {
//////        val i: Int = super.getBrightnessForRender(partialTick)
//////        var j = 0
//////        if (world.isBlockLoaded(BlockPos(sourcePos))) {
//////            j = world.getCombinedLight(BlockPos(sourcePos), 0)
//////        }
//////        return if (i == 0) j else i
//////    }
////
////    companion object {
////        const val TICKS_PER_SECOND = 20
////        const val PHASE_2_Y_END_POS_INCREASE_PER_TICK: Float = 0.5f / TICKS_PER_SECOND
////        const val SPEED_BLOCKS_PER_TICK_BASE: Float = 1.0f / TICKS_PER_SECOND
////        const val SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK: Double = 0.2f / TICKS_PER_SECOND / TICKS_PER_SECOND.toDouble()
////        const val PHASE_2_START_TICKS: Int = 5 * TICKS_PER_SECOND
////        const val SLAMDOWN_SPEED_BLOCKS_PER_TICK = 4f / 20
////    }
////
////    init {
////        this.setSprite (texture)
////        this.colorRed = 0.6f
////        this.colorGreen = 0.6f
////        this.colorBlue = 0.6f
////        this.scale /= 2.0f
////        sourcePos = startPos
////        endX = origEndPos.x
////        endZ = origEndPos.z
////        origEndY = origEndPos.y
////        this.maxAge = 500
////    }
//}