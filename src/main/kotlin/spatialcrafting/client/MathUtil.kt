package spatialcrafting.client

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import spatialcrafting.util.d


object MathUtil {
    /**
     * Returns the distance between 2 positions. counts diagonals as 1 distance (rather than 2 or 3)
     */
    fun minimalDistanceOf(pos1: Vec3i, pos2: Vec3i): Int {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.max(Math.abs(pos1.y - pos2.y), Math.abs(pos1.z - pos2.z)))
    }

    /**
     * Returns the distance between 2 positions. counts diagonals as 1 distance (rather than 2 or 3)
     */
    fun minimalDistanceOf(pos1: Vec3d, pos2: Vec3d): Double {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.max(Math.abs(pos1.y - pos2.y), Math.abs(pos1.z - pos2.z)))
    }

    /**
     * Returns the distance between 2 Vec3d. Counts diagonals as the actual physical distance (rather than just 1)
     */
    fun euclideanDistanceOf(pos1: Vec3d, pos2: Vec3d?): Double {
        return norm(pos1.subtract(pos2))
    }

    fun euclideanDistanceOf(pos1: Vec3i, pos2: Vec3i): Double {
        return euclideanDistanceOf(Vec3d(pos1.x.d, pos1.y.d, pos2.z.d), Vec3d(pos2.x.d, pos2.y.d, pos2.z.d))
    }

    private fun norm(vec3: Vec3d): Double {
        return Math.sqrt(vec3.x * vec3.x + vec3.y * vec3.y + vec3.z * vec3.z)
    }

    /**
     * Returns the position at the middle of the 2 BlockPoses.
     */
    fun middleOf(pos1: Vec3d, pos2: Vec3d): Vec3d? {
        val x: Double = (pos1.x + pos2.x) / 2.0
        val y: Double = (pos1.y + pos2.y) / 2.0
        val z: Double = (pos1.z + pos2.z) / 2.0
        return Vec3d(x, y, z)
    }

    fun middleOf(pos1: Vec3i?, pos2: Vec3i?): Vec3d? {
        return middleOf(Vec3d(pos1), Vec3d(pos2))
    }
}