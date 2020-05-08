package spatialcrafting.compat.rei

import com.mojang.blaze3d.systems.RenderSystem
import fabricktx.api.getMinecraftClient
import fabricktx.api.playButtonClickSound
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.gui.widget.WidgetWithBounds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.Element
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

open class ReiButton(
        private var x: Int, private var y: Int, private val width: Int, private val height: Int,
        private val textureOn: Identifier, private val textureOff: Identifier,
        val isEnabled: (MinecraftClient) -> Boolean = { true }, val onClick: (MinecraftClient) -> Unit = {}
) : WidgetWithBounds() {

    override fun render(stack: MatrixStack,mouseX: Int, mouseY: Int, delta: Float) {
        if (isEnabled(minecraft)) {

            if (containsMouse(mouseX, mouseY)) {
                emitHoverColor()
            } else {
                clearColor()
            }

            // Draw on texture
            minecraft.textureManager.bindTexture(textureOn)
            DrawableHelper.drawTexture(stack, x, y, 0f, 0f, width, height, width, height)


        } else {
            clearColor()

            // Draw off texture
            minecraft.textureManager.bindTexture(textureOff)
            DrawableHelper.drawTexture(stack,x, y, 0f, 0f, width, height, width, height)
        }
    }

    override fun children(): List<Element> = listOf()


    override fun getBounds(): Rectangle = Rectangle(x, y, width, height)


    override fun mouseClicked(mouseX: Double, mouseY: Double, buttonId: Int): Boolean {
        if (containsMouse(mouseX, mouseY) && isEnabled(minecraft) && buttonId == 0) {
            getMinecraftClient().playButtonClickSound()
            onClick(minecraft)
            return true
        }
        return false

    }


}

fun emitHoverColor() {
    RenderSystem.color4f(0.7f, 0.7f, 1.0f, 1.0f)
}

fun clearColor() {
    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
}