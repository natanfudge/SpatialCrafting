package spatialcrafting.util

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengl.GL11

class GL {
    val minecraft: MinecraftClient = MinecraftClient.getInstance()

    companion object {
        /**
         * Starting point for gl calls
         */
        inline fun begin(rendering: GL.() -> Unit) {
            GlStateManager.pushMatrix()
            GL().apply(rendering)
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
     * Scales the xsize, ysize and zsize of the model by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    inline fun scale(x: Int, y: Int, z: Int, code: () -> Unit) {
        GlStateManager.scalef(x.toFloat(), y.toFloat(), z.toFloat())
        code()
        GlStateManager.scalef(1f, 1f, 1f)
    }

    /**
     * Scales the xsize, ysize and zsize of the model by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    inline fun scale(x: Double, y: Double, z: Double, code: () -> Unit) {
        GlStateManager.scaled(x, y, z)
        code()
        GlStateManager.scalef(1f, 1f, 1f)
    }


    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     */
    fun rotate(angle: Float, x: Int, y: Int, z: Int) {
        GL11.glRotatef(angle, x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     */
    fun rotate(angle: Float, x: Int, y: Double, z: Int) {
        GL11.glRotatef(angle, x.toFloat(), y.toFloat(), z.toFloat())
    }

}