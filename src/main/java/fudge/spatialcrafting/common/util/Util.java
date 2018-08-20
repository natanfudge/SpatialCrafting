package fudge.spatialcrafting.common.util;


import fudge.spatialcrafting.SpatialCrafting;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public final class Util {

    private Util() {}

    /**
     * Returns the distance between 2 positions. counts diagonals as 1 distance (rather than 2 or 3)
     */
    public static int minimalDistanceOf(Vec3i pos1, Vec3i pos2) {
        return Math.max(Math.abs(pos1.getX() - pos2.getX()), Math.max(Math.abs(pos1.getY() - pos2.getY()), Math.abs(pos1.getZ() - pos2.getZ())));
    }


    /**
     * Returns the distance between 2 positions. counts diagonals as 1 distance (rather than 2 or 3)
     */
    public static double minimalDistanceOf(Vec3d pos1, Vec3d pos2) {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.max(Math.abs(pos1.y - pos2.y), Math.abs(pos1.z - pos2.z)));
    }

    /**
     * Returns the distance between 2 Vec3d. Counts diagonals as the actual physical distance (rather than just 1)
     */
    public static double euclideanDistanceOf(Vec3d pos1, Vec3d pos2) {
        return norm(pos1.subtract(pos2));
    }

    private static double norm(Vec3d vec3) {
        return Math.sqrt(vec3.x * vec3.x + vec3.y * vec3.y + vec3.z * vec3.z);
    }


    /**
     * Returns the position at the middle of the 2 BlockPoses.
     */
    public static Vec3d middleOf(Vec3d pos1, Vec3d pos2) {
        double x = (pos1.x + pos2.x) / 2.0;
        double y = (pos1.y + pos2.y) / 2.0;
        double z = (pos1.z + pos2.z) / 2.0;

        return new Vec3d(x, y, z);
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
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     *
     * @param tester The condition in which two inner objects are considered equal
     */
    public static <T> boolean innerEquals(T[][][] arr1, T[][][] arr2, BiPredicate<T, T> tester) {
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr1[i].length; j++) {
                for (int k = 0; k < arr1[i][j].length; k++) {

                    T object1 = arr1[i][j][k];
                    T object2 = arr2[i][j][k];

                    if (object1 != null && object2 != null) {
                        if (!tester.test(object1, object2)) {
                            return false;
                        }
                        // If one of them are null then we have to use reference comparison
                    } else if ((object1 != object2)) {
                        return false;
                    }
                }

            }
        }

        return true;
    }

    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     */
    public static boolean innerEquals(Object[][][] arr1, Object[][][] arr2) {
        return innerEquals(arr1, arr2, Object::equals);
    }


    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     *
     * @param tester The condition in which two inner objects are considered equal
     */
    public static <T1, T2> boolean innerEqualsDifferentSizes(T1[][][] arr1, T2[][][] arr2, BiPredicate<T1, T2> tester) {
        return innerEqualsDifferentSizes(arr1, arr2, tester, null);
    }

    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Counts null the same as the array being not in bounds, allowing for different sizes of arrays to be equal.
     *
     * @param tester     The condition in which two inner objects are considered equal
     * @param nullObject An object that should count as equal to null.
     */
    public static <T1, T2, T3> boolean innerEqualsDifferentSizes(T1[][][] arr1, T2[][][] arr2, BiPredicate<T1, T2> tester, T3 nullObject) {
        for (int i = 0; i < Math.max(arr1.length, arr2.length); i++) {

            // Out of bounds handling
            if (i >= arr1.length) {
                if (areaIsNull(arr2[i])) {
                    continue;   // This is fine, this area counts as equal
                } else {
                    return false; // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                }
            }
            if (i >= arr2.length) {
                if (areaIsNull(arr1[i])) {
                    continue;
                } else {
                    return false;
                }
            }

            for (int j = 0; j < Math.max(arr1[i].length, arr2[i].length); j++) {

                // Out of bounds handling
                if (j >= arr1[i].length) {
                    if (areaIsNull(arr2[i][j])) {
                        continue;   // This is fine, this area counts as equal
                    } else {
                        return false;   // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                    }
                }
                if (j >= arr2[i].length) {
                    if (areaIsNull(arr1[i][j])) {
                        continue;
                    } else {
                        return false;
                    }
                }

                for (int k = 0; k < Math.max(arr1[i][j].length, arr2[i][j].length); k++) {

                    // Out of bounds handling
                    if (k >= arr1[i][j].length) {
                        if (arr2[i][j][k] == null) {
                            continue;   // This is fine, this area counts as equal
                        } else {
                            return false;   // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                        }
                    }
                    if (k >= arr2[i][j].length) {
                        if (arr1[i][j][k] == null) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    T1 object1 = arr1[i][j][k];
                    T2 object2 = arr2[i][j][k];

                    // Null handling
                    if (object1 == null && object2 == null) continue;
                    if (object1 == null) {
                        if (object2.equals(nullObject)) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    if (object2 == null) {
                        if (object1.equals(nullObject)) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    // Finally the test

                    if (!tester.test(object1, object2)) {
                        return false;
                    }

                }

            }
        }

        return true;
    }

    private static <T> boolean areaIsNull(T[] areaOfArr) {
        for (T object : areaOfArr) {
            if (object != null) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean areaIsNull(T[][] areaOfArr) {
        for (T[] arr1D : areaOfArr) {
            for (T object : arr1D) {
                if (object != null) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Returns true if every inner element in a 3D array is equal to a singular object.
     */
    public static <T1, T2> boolean arrEqualsObj(T1[][][] arr1, T2 object, BiPredicate<T1, T2> tester) {
        for (T1[][] arr2D : arr1) {
            for (T1[] arr1D : arr2D) {
                for (T1 innerElement : arr1D) {
                    if (!tester.test(innerElement, object)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Perform an action on every inner object in a 3D array.
     *
     * @param function The action to perforn on every inner object.
     */
    public static <T> void innerForEach(T[][][] arr, Consumer<T> function) {
        for (T[][] arr2D : arr) {
            innerForEach2D(arr2D, function);

        }
    }

    public static <T> void innerForEach2D(T[][] arr, Consumer<T> function) {
        for (T[] arr1D : arr) {
            for (T object : arr1D) {
                function.accept(object);
            }
        }
    }


    /**
     * Gets the TileEntity at the given position in the world
     *
     * @param world The world to get the TileEntity from
     * @param pos   The position in the world to get the TileEntity from
     */
    @Nullable
    public static <T extends TileEntity> T getTileEntity(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {


        try {
            return (T) world.getTileEntity(pos);
        } catch (ClassCastException e) {
            SpatialCrafting.LOGGER.error("Invalid cast trying to cast between two different tile entities", e);
            return null;
        }

    }


    /**
     * Trick intellij into not being NPE tricked by the forge objectholder trick
     */
    @SuppressWarnings("ConstantConditions")
    public static <T> T notNull() {
        return null;
    }


}
