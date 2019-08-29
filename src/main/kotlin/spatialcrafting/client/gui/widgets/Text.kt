package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.widget.WLabel
import net.minecraft.client.MinecraftClient
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.runtimeWidget
import spatialcrafting.util.getMinecraftClient


private const val StringHeight = 8

//TODO: some funcy Draw{} shit that allows me to avoid creating a class

class TextClass(private val text: String, private val color: Int) : DevWidget() {
    override val minimumHeight = StringHeight
    override val minimumWidth = widthOf(text) - 1

    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints) {
        val render = getMinecraftClient().fontManager.getTextRenderer(MinecraftClient.DEFAULT_TEXT_RENDERER_ID)
        render!!.draw(text, constraints.x.toFloat() /*(constraints.x - minimumWidth / 2).toFloat()*/, constraints.y.toFloat(), color)
    }

}

//fun DevWidget.Text(text: Text, color: Int): DevWidget = add(TextClass(text), height = StringHeight))

fun DevWidget.Text(str: String, color: Int = WLabel.DEFAULT_TEXT_COLOR): DevWidget =
        add(TextClass(str, color))

//fun DevWidget.Text(str: String, color: Int = WLabel.DEFAULT_TEXT_COLOR): DevWidget =
//        Text(LiteralText(str), color)

private fun widthOf(str: String) = getMinecraftClient().textRenderer.getStringWidth(str)
//        .also { logDebug { "The width of '$str' is $it pixels" } }