package fudge.spatialcrafting.common.util;


import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketRemoveTileEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public final class Util {

    private Util() {}

    public static void removeTileEntity(World world, BlockPos pos, boolean syncClient) {
        world.removeTileEntity(pos);
        if (syncClient) {
            PacketHandler.getNetwork().sendToDimension(new PacketRemoveTileEntity(pos), world.provider.getDimension());
        }

    }


    /**
     * Drops an itemStack in the world
     *
     * @param world     The world to drop the itemStack in
     * @param pos       The position in the world to drop the itemStack in
     * @param itemStack The itemStack to drop in the world.
     */
    public static void dropItemStack(World world, Vec3d pos, ItemStack itemStack) {
        dropItemStack(world, pos, itemStack, true);
    }

    public static void dropItemStack(World world, Vec3d pos, ItemStack itemStack, boolean randomMotion) {
        EntityItem itemEntity = new EntityItem(world, pos.x, pos.y, pos.z, itemStack);
        if (!randomMotion) itemEntity.motionX = itemEntity.motionY = itemEntity.motionZ = 0;
        world.spawnEntity(itemEntity);
    }


    /**
     * Gets the TileEntity at the given position in the world
     *
     * @param world The world to get the TileEntity from
     * @param pos   The position in the world to get the TileEntity from
     * @throws NullPointerException instead of returning null when there is no tileEntity at the specified position
     * @throws ClassCastException   if the tile entity type that was requested does not match the one that exists at the specified position
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends TileEntity> T getTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {

        TileEntity tileEntity = world.getTileEntity(pos);
        try {
            if (tileEntity != null) {
                return (T) tileEntity;
            } else {
                throw new NullPointerException("Attempt to get a tile entity at a position in which there is none");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Invalid cast trying to cast between two different tile entities");
        }
    }

    /**
     * Trick intellij into not being NPE tricked by the forge objectHolder trick
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    public static <T> T objectHolder() {
        return null;
    }

    public static String translate(String key) {
        TextComponentTranslation translatedText = new TextComponentTranslation(key);
        return translatedText.getUnformattedComponentText();
    }

}
