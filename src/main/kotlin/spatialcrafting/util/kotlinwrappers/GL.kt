package spatialcrafting.util.kotlinwrappers

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengl.GL11

class GL {
    val minecraft: MinecraftClient = MinecraftClient.getInstance()
    var scaled: Boolean = false

    companion object {
        /**
         * Starting point for gl calls
         */
        inline fun begin(rendering: GL.() -> Unit) {
            GlStateManager.pushMatrix()
            val gl = GL().apply(rendering)
            if (gl.scaled) GlStateManager.scalef(1f, 1f, 1f)
            GlStateManager.popMatrix()
        }
    }

    /**
     * Moves the model to the specified [x] [y] [z] coordinates.
     */
    fun translate(x: Double, y: Double, z: Double) {
        GlStateManager.translated(x, y, z)
    }

    /**
     * Moves the model to the specified [x] [y] [z] coordinates.
     */
    fun translate(x: Float, y: Float, z: Float) {
        GlStateManager.translatef(x, y, z)
    }

    /**
     * Moves the model to the specified [x] [y] [z] coordinates.
     */
    fun translate(x: Int, y: Int, z: Int) {
        GlStateManager.translatef(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * Scales the xsize, ysize and zsize by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    fun scale(x: Number, y: Number, z: Number) {
        GlStateManager.scaled(x.toDouble(), y.toDouble(), z.toDouble())
        scaled = true
    }

    /**
     * Scales the xsize, ysize and zsize by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    fun scale(x: Float, y: Float, z: Float) {
        GlStateManager.scalef(x, y, z)
        scaled = true
    }

    /**
     * Scales the xsize, ysize and zsize of the model by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    fun scale(x: Int, y: Int, z: Int) {
        GlStateManager.scalef(x.toFloat(), y.toFloat(), z.toFloat())
        scaled = true
    }

    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     * @param angle the angle of rotation in degrees
     * @param x     the x coordinate of the rotation vector
     * @param y     the y coordinate of the rotation vector
     * @param z     the z coordinate of the rotation vector
     */
    fun rotate(angle: Double, x: Double, y: Double, z: Double) {
        GlStateManager.rotated(angle, x, y, z)
    }

    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     * @param angle the angle of rotation in degrees
     * @param x     the x coordinate of the rotation vector
     * @param y     the y coordinate of the rotation vector
     * @param z     the z coordinate of the rotation vector
     */
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        GlStateManager.rotatef(angle, x, y, z)
    }

    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     * @param angle the angle of rotation in degrees
     * @param x     the x coordinate of the rotation vector
     * @param y     the y coordinate of the rotation vector
     * @param z     the z coordinate of the rotation vector
     */
    fun rotate(angle: Float, x: Int, y: Int, z: Int) {
        GL11.glRotatef(angle, x.toFloat(), y.toFloat(), z.toFloat())
    }

}