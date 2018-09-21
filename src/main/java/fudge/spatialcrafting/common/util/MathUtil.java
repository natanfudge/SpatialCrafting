package fudge.spatialcrafting.common.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class MathUtil {

    private MathUtil() {}

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

    public static double euclideanDistanceOf(Vec3i pos1, Vec3i pos2) {
        return euclideanDistanceOf(new Vec3d(pos1.getX(), pos1.getY(), pos2.getZ()), new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ()));
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


    public static Vec3d middleOf(Vec3i pos1, Vec3i pos2) {
        return middleOf(new Vec3d(pos1), new Vec3d(pos2));
    }

}
