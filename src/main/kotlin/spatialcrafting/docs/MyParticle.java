//package spatialcrafting.docs;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.particle.BillboardParticle;
//import net.minecraft.client.particle.Particle;
//import net.minecraft.client.particle.SpriteBillboardParticle;
//import net.minecraft.client.texture.Sprite;
//import net.minecraft.client.texture.Texture;
//import net.minecraft.util.Identifier;
//import net.minecraft.world.World;
//
//import spatialcrafting.SpatialCraftingKt;
//
//public class FlameParticle extends SpriteBillboardParticle {
//    private static final Identifier resourceLocation = new Identifier(SpatialCraftingKt.ModId,"entity/flame_fx");
//
//    /**
//     * Construct a new FlameParticle at the given [x,y,z] position with the given initial velocity.
//     */
//    public FlameParticle(World world, double x, double y, double z,
//                         double velocityX, double velocityY, double velocityZ) {
//        super(world, x, y, z, velocityX, velocityY, velocityZ);
//
//
//        maxAge = 100; // not used since we have overridden onUpdate
//
//
//        // set the texture to the flame texture, which we have previously added using TextureStitchEvent
//        //   (see TextureStitcherBreathFX)
//        Texture texture = MinecraftClient.getInstance().getTextureManager().getTexture(resourceLocation);
//
//        setParticleTexture(sprite);  // initialise the icon to our custom texture
//    }
//
//    /**
//     * Used to control what texture and lighting is used for the EntityFX.
//     * Returns 1, which means "use a texture from the blocks + items texture sheet"
//     * The vanilla layers are:
//     * normal particles: ignores world brightness lighting map
//     * Layer 0 - uses the particles texture sheet (textures\particle\particles.png)
//     * Layer 1 - uses the blocks + items texture sheet
//     * lit particles: changes brightness depending on world lighting i.e. block light + sky light
//     * Layer 3 - uses the blocks + items texture sheet (I think)
//     *
//     * @return
//     */
//    @Override
//    public int getFXLayer() {
//        return 1;
//    }
//
//    // can be used to change the brightness of the rendered Particle.
//    @Override
//    public int getBrightnessForRender(float partialTick) {
//        final int FULL_BRIGHTNESS_VALUE = 0xf000f0;
//        return FULL_BRIGHTNESS_VALUE;
//
//        // if you want the brightness to be the local illumination (from block light and sky light) you can just use
//        //  Entity.getBrightnessForRender() base method, which contains:
//        //    BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
//        //    return this.worldObj.isBlockLoaded(blockpos) ? this.worldObj.getCombinedLight(blockpos, 0) : 0;
//    }
//
//    // this function is used by ParticleManager.addEffect() to determine whether depthmask writing should be on or not.
//    // FlameBreathFX uses alphablending (i.e. the FX is partially transparent) but we want depthmask writing on,
//    //   otherwise translucent objects (such as water) render over the top of our breath, even if the particle is in front
//    //  of the water and not behind
//    @Override
//    public boolean shouldDisableDepth() {
//        return false;
//    }
//
//    /**
//     * call once per tick to update the Particle position, calculate collisions, remove when max lifetime is reached, etc
//     */
//    @Override
//    public void onUpdate() {
//        prevPosX = posX;
//        prevPosY = posY;
//        prevPosZ = posZ;
//
//        move(motionX, motionY, motionZ);  // simple linear motion.  You can change speed by changing motionX, motionY,
//        // motionZ every tick.  For example - you can make the particle accelerate downwards due to gravity by
//        // final double GRAVITY_ACCELERATION_PER_TICK = -0.02;
//        // motionY += GRAVITY_ACCELERATION_PER_TICK;
//
//        // collision with a block makes the ball disappear.  But does not collide with entities
//        if (onGround) {  // onGround is only true if the particle collides while it is moving downwards...
//            this.setExpired();
//        }
//
//        if (prevPosY == posY && motionY > 0) {  // detect a collision while moving upwards (can't move up at all)
//            this.setExpired();
//        }
//
//        if (this.particleMaxAge-- <= 0) {
//            this.setExpired();
//        }
//    }
//
//
//}