package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.GuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import spatialcrafting.ModId
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.util.playButtonClickSoundToClient

fun DevWidget.Image(texture: Identifier, width: Int, height: Int): DevWidget = LibGuiWidget(WSprite(texture), width, height)
fun DevWidget.Image(path: String, width: Int, height: Int): DevWidget = Image(Identifier("$ModId:textures/$path"), width, height)

fun DevWidget.Switch(enabled: Boolean): DevWidget = LibGuiWidget(WToggleButton().apply { toggle = enabled })

fun DevWidget.Button(text: String, enabled: Boolean = true, onClick: () -> Unit): DevWidget = LibGuiWidget(
        WButton(LiteralText(text)).apply { isEnabled = enabled }, width = widthOf(text) + 5, height = 20)
        .onClick {
            if (enabled) {
                playButtonClickSoundToClient()
                onClick()
            }
        }

fun DevWidget.TextField(hintText: String = "", defaultText: String = "", description: GuiDescription, width: Int, onChange: (String) -> Unit) =
        LibGuiWidget(object : WTextField(LiteralText(hintText)) {
            override fun onCharTyped(ch: Char) {
                super.onCharTyped(ch)
                onChange(text)
            }

            override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
                super.onKeyPressed(ch, key, modifiers)
                // Invoke callback when character was deleted
                if (modifiers == 0 && (ch == GLFW.GLFW_KEY_DELETE || ch == GLFW.GLFW_KEY_BACKSPACE)) {
                    onChange(text)
                }
            }
        }.apply {
            createPeers(description)
            text = defaultText
        }, width = width)
                .onClick { this.libGuiWidget.onClick(getClientMouseX(), getClientMouseY(), -1) }
