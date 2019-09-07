package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.widget.WLabel
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.widgets.core.Overlay
import spatialcrafting.util.getMinecraftClient


const val StringHeight = 8

class TextClass(private val text: String, private val color: Int, overlay: Overlay?) : NoChildDevWidget(overlay) {
    override val composeDirectChildren: DevWidget.() -> Unit = {}
    override val minimumHeight = StringHeight
    override val minimumWidth = widthOf(text) - 1


    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints) {
        val renderer = getMinecraftClient().fontManager.getTextRenderer(MinecraftClient.DEFAULT_TEXT_RENDERER_ID)

        // Need to increase the width we put into the wrap function because it insists on requiring more space, which means
        // the last word is always put on the next line, unless we give it more width.
        renderer!!.wrapStringToWidthAsList(text, constraints.width + 4).forEachIndexed { lineNum, line ->
            renderer.draw(line, constraints.x.toFloat(), constraints.y.toFloat() + (StringHeight + 2) * lineNum, color)
        }


    }

}


fun DevWidget.Text(str: String, color: Int = WLabel.DEFAULT_TEXT_COLOR): DevWidget =
        add(TextClass(str, color, overlay))

fun DevWidget.Text(vararg strings: String) {
    for (i in 0 until strings.size - 1) {
        Text(strings[i])
        VerticalSpace(1)
    }
    Text(strings[strings.size - 1])
}

fun widthOf(str: String) = getMinecraftClient().textRenderer.getStringWidth(str)
