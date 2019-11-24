package spatialcrafting.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f

class GL(val matrixStack: MatrixStack) {
    val minecraft: MinecraftClient = MinecraftClient.getInstance()

    companion object {
        /**
         * Starting point for gl calls
         */
        inline fun begin(matrixStack: MatrixStack, rendering: GL.() -> Unit) {
            matrixStack.push()
            GL(matrixStack).apply(rendering)
            matrixStack.pop()
        }
    }

    /**
     * Moves the model to the specified [x] [y] [z] coordinates.
     */
    fun translate(x: Double, y: Double, z: Double) {
        matrixStack.translate(x, y, z)
    }


    /**
     * Scales the xsize, ysize and zsize of the model by the [x] [y] [z] multipliers (1 = normal size, 0.5 = half size, etc)
     */
    inline fun scale(x: Int, y: Int, z: Int, code: () -> Unit) = scale(x.f, y.f, z.f, code)

    inline fun scale(x: Float, y: Float, z: Float, code: () -> Unit) {
        matrixStack.scale(x, y, z)
        code()
        matrixStack.scale(1f, 1f, 1f)
    }


    /**
     * Rotates the model's [x] [y] and [z] axis by the specified amount.
     */
    fun rotateY(angle: Float) {
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle))
    }


}