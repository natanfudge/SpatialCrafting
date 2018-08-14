package fudge.spatialcrafting.client.particle;

import fudge.spatialcrafting.client.tick.ClientTicker;
import fudge.spatialcrafting.common.SCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static fudge.spatialcrafting.common.SCConstants.TICKS_PER_SECOND;
import static fudge.spatialcrafting.common.block.BlockCrafter.CRAFT_DURATION_MULTIPLIER;


@SideOnly(Side.CLIENT)
//TODO: have the particles slam down at the end
//TODO sound (also have a BANG when it slams down)
public class ParticleItemDust extends Particle {

    public static final float PHASE_2_SPEED_BLOCKS_PER_TICK_UPWARDS = 0.5f / TICKS_PER_SECOND;
    public static final int PHASE_2_START_TICKS = 0 * TICKS_PER_SECOND;
    private static final String TICKER_ID = "ticker_particle_item_dust";
    private static final float SPEED_BLOCKS_PER_TICK = 1.0f / TICKS_PER_SECOND;
    private static final int TICKS_BETWEEN_PARTICLES = (int) (0.1f * TICKS_PER_SECOND);
    //private static final Function<Double,Double> DISTANCE_TO_TIME = (distance) -> 2 * distance / Math.sqrt(3);

    private static int distanceToTime(double distance){
        return (int)(1 * (distance * TICKS_PER_SECOND) / Math.sqrt(3));
    }

    private static double preciseDistance(double distance){
        return 1 * (distance * TICKS_PER_SECOND) / Math.sqrt(3);
    }

   /* private static double posesToTime(Vec3d pos1, Vec3d pos2){
        double degree = Math.asin((pos1.subtract(pos2)).normalize().x);


    }*/


    private Vec3d sourcePos;
    private Vec3d designation;

    public ParticleItemDust(World worldIn, Vec3d startPos, Vec3d designationPos, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite texture) {
        super(worldIn, startPos.x, startPos.y, startPos.z, xSpeed, ySpeed, zSpeed);
        this.setParticleTexture(texture);
        this.particleRed = 0.6F;
        this.particleGreen = 0.6F;
        this.particleBlue = 0.6F;
        this.particleScale /= 2.0F;

        // The vanilla Particle constructor added random variation to our starting velocity.  Undo it!
        this.motionX = xSpeed;
        this.motionY = ySpeed;
        this.motionZ = zSpeed;

        this.sourcePos = startPos;
        this.particleMaxAge = 500;
        this.designation = designationPos;
    }

    public static ParticleItemDust create(World world, Vec3d startPos, Vec3d endPos, double blocksPerTick, ItemStack itemStack) {
        Vec3d direction = endPos.subtract(new Vec3d(startPos.x, startPos.y, startPos.z)).normalize();

        double speedX = direction.x * blocksPerTick;
        double speedY = direction.y * blocksPerTick;
        double speedZ = direction.z * blocksPerTick;

        TextureAtlasSprite texture = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemStack,
                world,
                null).getParticleTexture();
        ParticleItemDust particle = new ParticleItemDust(world, startPos, endPos, speedX, speedY, speedZ, texture);

        return particle;
    }

    //TODO: fix this. maybe make it faster as it goes on.
    public static void playCraftParticles(World world, BlockPos crafterPos) {


        TileCrafter crafter = Util.getTileEntity(world, crafterPos);
        assert crafter != null;


        int durationTicks = crafter.getCrafterSize() * CRAFT_DURATION_MULTIPLIER * SCConstants.TICKS_PER_SECOND;

        ClientTicker.addTicker(ticksPassed -> {


                    Util.innerForEach(crafter.getHolograms(), hologramPos -> {
                        TileEntity hologramTile = Util.getTileEntity(world, hologramPos);
                        IItemHandler itemHandler = hologramTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
                        ItemStack itemStack = itemHandler.getStackInSlot(0);

                        if (!itemStack.getItem().equals(Items.AIR)) {
                            Vec3d startPos = new Vec3d(hologramPos.getX() + 0.5, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.5);

                            Vec3d endPos = crafter.centerOfHolograms();

                            int extra = distanceToTime(Util.distanceOf(startPos, endPos));
                            int relativeTicksPassed = ticksPassed + extra;

                            if (relativeTicksPassed >= PHASE_2_START_TICKS) {
                                double ticksSincePhase2 = relativeTicksPassed - PHASE_2_START_TICKS;
                                //double newY = endPos.y + (ticksSincePhase2) * PHASE_2_SPEED_BLOCKS_PER_TICK_UPWARDS;



                                double newY = endPos.y + preciseDistance(Util.distanceOf(startPos, endPos));

                                endPos = new Vec3d(endPos.x, newY, endPos.z);
                            }

                            if (durationTicks > relativeTicksPassed) {


                                shootDustParticle(world, startPos, endPos, itemStack);

                            }
                        }


/*                        TileEntity hologramTile = Util.getTileEntity(world, hologramPos);
                        IItemHandler itemHandler = hologramTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
                        ItemStack itemStack = itemHandler.getStackInSlot(0);

                        if (!itemStack.getItem().equals(Items.AIR)) {
                            Vec3d startPos = new Vec3d(hologramPos.getX() + 0.2, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.2);

                            Vec3d endPos = crafter.centerOfHolograms();

                            int extra = (int) (Util.minimalDistanceOf(startPos, endPos) / SPEED_BLOCKS_PER_TICK);
                            int relativeTicksPassed = ticksPassed + extra;

                            if (relativeTicksPassed >= PHASE_2_START_TICKS) {
                                double ticksSincePhase2 = relativeTicksPassed - PHASE_2_START_TICKS;
                                double newY = endPos.y + (ticksSincePhase2) * PHASE_2_SPEED_BLOCKS_PER_TICK_UPWARDS;
                                endPos = new Vec3d(endPos.x, newY, endPos.z);
                            }

                            extra = (int) (Util.minimalDistanceOf(startPos, endPos) / SPEED_BLOCKS_PER_TICK);
                            relativeTicksPassed = ticksPassed + extra;


                            if (durationTicks > relativeTicksPassed) {


                                shootDustParticle(world, startPos, endPos, itemStack);

                                startPos = new Vec3d(hologramPos.getX() + 0.2, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.8);
                                shootDustParticle(world, startPos, endPos, itemStack);

                                startPos = new Vec3d(hologramPos.getX() + 0.8, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.2);
                                shootDustParticle(world, startPos, endPos, itemStack);

                                startPos = new Vec3d(hologramPos.getX() + 0.8, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.8);
                                shootDustParticle(world, startPos, endPos, itemStack);
                            }
                        }*/
                    });
                }, TICKS_BETWEEN_PARTICLES,
                durationTicks,
                ParticleItemDust.TICKER_ID + Util.<TileCrafter>getTileEntity(world, crafterPos).getMasterPos());

    }

    @SideOnly(Side.CLIENT)
    private static void shootDustParticle(World world, Vec3d startPos, Vec3d endPos, ItemStack stack) {
        Minecraft.getMinecraft().effectRenderer.addEffect(ParticleItemDust.create(world, startPos, endPos, SPEED_BLOCKS_PER_TICK, stack));
    }

    public static void stopParticles(TileCrafter tile) {
        ClientTicker.stopTickers(TICKER_ID + tile.getMasterPos());
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void onUpdate() {


        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;


        if (Util.distanceOf(designation, new Vec3d(posX, posY, posZ)) < 0.1) {
            this.setExpired();
        }

        if (this.particleAge++ >= this.particleMaxAge) {
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
