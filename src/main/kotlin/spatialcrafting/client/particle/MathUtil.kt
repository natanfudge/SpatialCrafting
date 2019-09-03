package spatialcrafting.client.particle

import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.sqrt


object MathUtil {
    /**
     * Returns the distance between 2 positions. counts diagonals as 1 distance (rather than 2 or 3)
     */
    fun minimalDistanceOf(pos1: Vec3d, pos2: Vec3d): Double {
        return abs(pos1.x - pos2.x).coerceAtLeast(abs(pos1.y - pos2.y).coerceAtLeast(abs(pos1.z - pos2.z)))
    }

    /**
     * Returns the distance between 2 Vec3d. Counts diagonals as the actual physical distance (rather than just 1)
     */
    fun euclideanDistanceOf(pos1: Vec3d, pos2: Vec3d?): Double {
        return norm(pos1.subtract(pos2))
    }

    private fun norm(vec3: Vec3d): Double {
        return sqrt(vec3.x * vec3.x + vec3.y * vec3.y + vec3.z * vec3.z)
    }

}