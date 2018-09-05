package fudge.spatialcrafting.common.util;


import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketRemoveTileEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

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
        EntityItem itemEntity = new EntityItem(world, pos.x, pos.y, pos.z, itemStack);
        world.spawnEntity(itemEntity);
    }


    /**
     * Gets the TileEntity at the given position in the world
     *
     * @param world The world to get the TileEntity from
     * @param pos   The position in the world to get the TileEntity from
     */
    @Nonnull
    public static <T extends TileEntity> T getTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {


        try {
            return (T) world.getTileEntity(pos);
        } catch (ClassCastException e) {
            throw new ClassCastException("Invalid cast trying to cast between two different tile entities");
        }

    }

    /**
     * Trick intellij into not being NPE tricked by the forge objectholder trick
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
