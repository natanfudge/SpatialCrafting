@file:Suppress("DEPRECATION")

package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.widget.WWidget
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.widgets.core.Overlay
import spatialcrafting.util.getMinecraftClient

private val mc = getMinecraftClient()


fun getClientMouseX() = (mc.mouse.x * mc.window.scaledWidth.toDouble() / mc.window.width.toDouble()).toInt()
fun getClientMouseY() = (mc.mouse.y * mc.window.scaledHeight.toDouble() / mc.window.height.toDouble()).toInt()

class LibGuiWidgetClass(val libGuiWidget: WWidget,
                        width: Int = libGuiWidget.width,
                        height: Int = libGuiWidget.height, overlay: Overlay?) : DevWidget(overlay) {
    override val composeDirectChildren: DevWidget.() -> Unit = { }

    override val minimumHeight = height
    override val minimumWidth = width

    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints) {
        libGuiWidget.paintBackground(constraints.x, constraints.y)
        libGuiWidget.paintForeground(constraints.x, constraints.y, getClientMouseX(), getClientMouseY())
    }.also { libGuiWidget.setSize(constraints.width, constraints.height) }


}

fun DevWidget.LibGuiWidget(libGuiWidget: WWidget, width: Int = libGuiWidget.width, height: Int = libGuiWidget.height): LibGuiWidgetClass = add(LibGuiWidgetClass(libGuiWidget, width, height, overlay))


fun DevWidget.runtimeWidget(constraints: Constraints,
                            children: List<RuntimeWidget> = listOf(),
                            debugIdentifier: String = "RuntimeWidget",
                            drawer: (RuntimeWidget) -> Unit) = object : RuntimeWidget {
    override var runtimeChildren = children

    override fun draw() = drawer(this)
    override val constraints = constraints
    override val origin = this@runtimeWidget
    override val debugIdentifier = debugIdentifier


}

abstract class NoChildDevWidget(overlay: Overlay?) : DevWidget(overlay) {
    override val composeDirectChildren: DevWidget.() -> Unit = {}
}