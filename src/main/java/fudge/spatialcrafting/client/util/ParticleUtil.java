package fudge.spatialcrafting.client.util;

import fudge.spatialcrafting.client.particle.ParticleCraft;
import fudge.spatialcrafting.client.tick.ClientTicker;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.TileHologram;
import fudge.spatialcrafting.common.util.ArrayUtil;
import fudge.spatialcrafting.common.util.MathUtil;
import fudge.spatialcrafting.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static fudge.spatialcrafting.client.particle.ParticleCraft.*;
import static fudge.spatialcrafting.common.MCConstants.TICKS_PER_SECOND;
import static fudge.spatialcrafting.common.block.BlockCrafter.CRAFT_DURATION_MULTIPLIER;

public class ParticleUtil {

    private static final int TICKS_BETWEEN_PARTICLES = (int) (0.1f * TICKS_PER_SECOND);
    private static final String TICKER_ID = "ticker_particle_item_dust";

    public static void playCraftParticles(World world, BlockPos crafterPos) {


        TileCrafter crafter = Util.getTileEntity(world, crafterPos);

        int durationTicks = crafter.size() * CRAFT_DURATION_MULTIPLIER * MCConstants.TICKS_PER_SECOND;

        // Send these particle every so often using a ticker
        ClientTicker.addTicker(ticksPassed -> {


            // Go through each hologram and send particles from it
            ArrayUtil.innerForEach(crafter.getHolograms(), hologramPos -> {
                TileHologram hologramTile = Util.getTileEntity(world, hologramPos);
                ItemStack itemStack = hologramTile.getStoredItem();

                if (!itemStack.getItem().equals(Items.AIR)) {

                    // Calculate start and end positions
                    Vec3d startPos = new Vec3d(hologramPos.getX() + 0.2, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.2);

                    Vec3d endPos = crafter.centerOfHolograms();


                    if (durationTicks > getRelativeTicksPassed(ticksPassed, startPos, endPos)) {

                        // Shot particles from the 4 corners of the hologram
                        shootCraftParticle(world, startPos, endPos, ticksPassed, durationTicks, itemStack);

                        startPos = new Vec3d(hologramPos.getX() + 0.2, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.8);
                        shootCraftParticle(world, startPos, endPos, ticksPassed, durationTicks, itemStack);

                        startPos = new Vec3d(hologramPos.getX() + 0.8, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.2);
                        shootCraftParticle(world, startPos, endPos, ticksPassed, durationTicks, itemStack);

                        startPos = new Vec3d(hologramPos.getX() + 0.8, hologramPos.getY() + 0.5, hologramPos.getZ() + 0.8);
                        shootCraftParticle(world, startPos, endPos, ticksPassed, durationTicks, itemStack);
                    }

                }
            });
        }, TICKS_BETWEEN_PARTICLES, durationTicks, TICKER_ID + Util.<TileCrafter>getTileEntity(world, crafterPos).masterPos());

    }

    public static Vec3d calcEndPos(int ticksPassed, Vec3d origEndPos) {
        final double endY;
        if (ticksPassed >= PHASE_2_START_TICKS) {
            final int phase2TicksPassed = ticksPassed - PHASE_2_START_TICKS;
            endY = origEndPos.y + (PHASE_2_Y_END_POS_INCREASE_PER_TICK * phase2TicksPassed);
        } else {
            endY = origEndPos.y;
        }

        return new Vec3d(origEndPos.x, endY, origEndPos.z);
    }

    public static double calcSpeed(int ticksPassed) {
        return SPEED_BLOCKS_PER_TICK_BASE + (SPEED_BLOCKS_PER_TICKS_INCREASE_PER_TICK * ticksPassed);
    }

    // This is used to make particles stop appearing slightly before the crafting stops, such that it looks like once ALL particles have stopped the crafting is done.
    private static int getRelativeTicksPassed(int ticksPassed, Vec3d startPos, Vec3d origEndPos) {

        Vec3d endPos = calcEndPos(ticksPassed, origEndPos);

        return (int) (ticksPassed + MathUtil.minimalDistanceOf(startPos, endPos) / calcSpeed(ticksPassed));

    }

    public static void stopCraftParticles(TileCrafter tile) {
        ClientTicker.stopTickers(TICKER_ID + tile.masterPos());
    }

    @SideOnly(Side.CLIENT)
    private static void shootCraftParticle(World world, Vec3d startPos, Vec3d endPos, int startTimeDelay, int craftTime, ItemStack stack) {
        TextureAtlasSprite texture = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null).getParticleTexture();
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCraft(world, startPos, endPos, startTimeDelay, craftTime, texture));
    }
}
