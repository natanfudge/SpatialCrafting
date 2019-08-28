package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.widget.WLabel
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.LibGuiWidget
import spatialcrafting.client.gui.WidgetContext
import spatialcrafting.util.getMinecraftClient


private const val StringHeight = 8


fun WidgetContext.Text(text: Text, color: Int) : DevWidget
        = LibGuiWidget(WLabel(text, color), width = widthOf(text.asFormattedString()), height = StringHeight)

fun WidgetContext.Text(str: String, color: Int = WLabel.DEFAULT_TEXT_COLOR): DevWidget =
        add(Text(LiteralText(str), color))

private fun widthOf(str: String) = getMinecraftClient().textRenderer.getStringWidth(str)
//        .also { logDebug { "The width of '$str' is $it pixels" } }