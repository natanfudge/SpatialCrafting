package fudge.spatialcrafting.client.particle;

import fudge.spatialcrafting.client.util.ParticleUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static fudge.spatialcrafting.common.util.MCConstants.TICKS_PER_SECOND;
import static fudge.spatialcrafting.common.util.MathUtil.euclideanDistanceOf;


//TODO particle improvement : when they reach the top, the particle stop appearing and they start circling that spot. Then after a bit they fall down quickly and explode to the sides.
@SideOnly(Side.CLIENT)
public class ParticleCraft extends Particle {

    public static final float PHASE_2_Y_END_POS_INCREASE_PER_TICK = 0.5f / TICKS_PER_SECOND;
    public static final float SPEED_BLOCKS_PER_TICK_BASE = 1.0f / TICKS_PER_SECOND;
    public static final double SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK = 0.2f / TICKS_PER_SECOND / TICKS_PER_SECOND;
    public static final int PHASE_2_START_TICKS = 5 * TICKS_PER_SECOND;
    public static final float SLAMDOWN_SPEED_BLOCKS_PER_TICK = 4f / 20;
    private final Vec3d sourcePos;
    private final double endZ;
    private final double endX;
    private final double origEndY;
    private final int craftDuration;
    private final double craftYEndPos;
    private int ticksPassed;

    public ParticleCraft(World worldIn, Vec3d startPos, Vec3d origEndPos, int startTimeDelay, int craftDuration, double craftYEndPos, TextureAtlasSprite texture) {
        super(worldIn, startPos.x, startPos.y, startPos.z, 0, 0, 0);
        this.setParticleTexture(texture);
        this.particleRed = 0.6F;
        this.particleGreen = 0.6F;
        this.particleBlue = 0.6F;
        this.particleScale /= 2.0F;

        this.sourcePos = startPos;
        this.endX = origEndPos.x;
        this.endZ = origEndPos.z;
        this.origEndY = origEndPos.y;
        this.ticksPassed = startTimeDelay;
        this.craftDuration = craftDuration;
        this.craftYEndPos = craftYEndPos;


        this.particleMaxAge = 500;
    }


    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    //optimize by making calcuations every second, or when a message is sent
    //optimize by making the endPos calculations at a single, seperate place (every tick), and then getting the result on each particle. This is probably not much...
    public void onUpdate() {


        final Vec3d endPos = ParticleUtil.calcEndPos(ticksPassed, craftDuration, new Vec3d(endX, origEndY, endZ), craftYEndPos);
        final Vec3d pos = new Vec3d(posX, posY, posZ);

        final Vec3d direction = (endPos.subtract(pos)).normalize();

        final double speed = ParticleUtil.calcSpeed(ticksPassed);

        motionX = direction.x * speed;
        motionY = direction.y * speed;
        motionZ = direction.z * speed;


        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;


        if (euclideanDistanceOf(endPos, pos) </* 0.1 + */speed) {
            this.setExpired();
        }

        if (this.ticksPassed++ >= 1000) {
            this.setExpired();
        }


        this.move(this.motionX, this.motionY, this.motionZ);


    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = ((float) this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
        float f1 = f + 0.015609375F;
        float f2 = ((float) this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
        float f3 = f2 + 0.015609375F;
        float f4 = 0.1F * this.particleScale;
        if (this.particleTexture != null) {
            f = this.particleTexture.getInterpolatedU((double) (this.particleTextureJitterX / 4.0F * 16.0F));
            f1 = this.particleTexture.getInterpolatedU((double) ((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F));
            f2 = this.particleTexture.getInterpolatedV((double) (this.particleTextureJitterY / 4.0F * 16.0F));
            f3 = this.particleTexture.getInterpolatedV((double) ((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F));
        }

        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & '\uffff';
        int k = i & '\uffff';
        buffer.pos((double) (f5 - rotationX * f4 - rotationXY * f4),
                (double) (f6 - rotationZ * f4),
                (double) (f7 - rotationYZ * f4 - rotationXZ * f4)).tex((double) f, (double) f3).color(this.particleRed,
                this.particleGreen,
                this.particleBlue,
                1.0F).lightmap(j, k).endVertex();
        buffer.pos((double) (f5 - rotationX * f4 + rotationXY * f4),
                (double) (f6 + rotationZ * f4),
                (double) (f7 - rotationYZ * f4 + rotationXZ * f4)).tex((double) f, (double) f2).color(this.particleRed,
                this.particleGreen,
                this.particleBlue,
                1.0F).lightmap(j, k).endVertex();
        buffer.pos((double) (f5 + rotationX * f4 + rotationXY * f4),
                (double) (f6 + rotationZ * f4),
                (double) (f7 + rotationYZ * f4 + rotationXZ * f4)).tex((double) f1, (double) f2).color(this.particleRed,
                this.particleGreen,
                this.particleBlue,
                1.0F).lightmap(j, k).endVertex();
        buffer.pos((double) (f5 + rotationX * f4 - rotationXY * f4),
                (double) (f6 - rotationZ * f4),
                (double) (f7 + rotationYZ * f4 - rotationXZ * f4)).tex((double) f1, (double) f3).color(this.particleRed,
                this.particleGreen,
                this.particleBlue,
                1.0F).lightmap(j, k).endVertex();
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int i = super.getBrightnessForRender(partialTick);
        int j = 0;
        if (this.world.isBlockLoaded(new BlockPos(this.sourcePos))) {
            j = this.world.getCombinedLight(new BlockPos(this.sourcePos), 0);
        }

        return i == 0 ? j : i;
    }

}
