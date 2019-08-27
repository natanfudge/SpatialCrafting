package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import me.shedaniel.rei.gui.widget.HighlightableWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.Element
import net.minecraft.util.Identifier
import spatialcrafting.util.playButtonClickSoundClient
import java.awt.Rectangle
import java.awt.Shape

open class ReiButton(
        private var x: Int, private var y: Int, private val width: Int, private val height: Int,
        private val textureOn: Identifier, private val textureOff: Identifier,
        val isEnabled: (MinecraftClient) -> Boolean = { true }, val onClick: (MinecraftClient) -> Unit = {}
) : HighlightableWidget() {

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        if (isEnabled(minecraft)) {

            if (isHighlighted(mouseX, mouseY)) {
                emitHoverColor()
            }
            else {
                clearColor()
            }

            // Draw on texture
            minecraft.textureManager.bindTexture(textureOn)
            DrawableHelper.blit(x, y, 0f, 0f, width, height, width, height)


        }
        else {
            clearColor()

            // Draw off texture
            minecraft.textureManager.bindTexture(textureOff)
            DrawableHelper.blit(x, y, 0f, 0f, width, height, width, height)
        }
    }

    override fun children(): List<Element> = listOf()


    override fun getBounds(): Shape = Rectangle(x, y, width, height)


    override fun mouseClicked(mouseX: Double, mouseY: Double, buttonId: Int): Boolean {
        if (isHighlighted(mouseX, mouseY) && isEnabled(minecraft) && buttonId == 0) {
            playButtonClickSoundClient()
            onClick(minecraft)
            return true
        }
        return false

    }


}

 fun emitHoverColor() {
    GlStateManager.color4f(0.7f, 0.7f, 1.0f, 1.0f)
}

fun clearColor() {
    GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
}