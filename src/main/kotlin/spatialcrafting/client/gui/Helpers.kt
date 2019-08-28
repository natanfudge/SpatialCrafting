package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.widget.WWidget
import spatialcrafting.util.getMinecraftClient

private val mc = getMinecraftClient()


fun getClientMouseX() = (mc.mouse.x * mc.window.scaledWidth.toDouble() / mc.window.width.toDouble()).toInt()
fun getClientMouseY() = (mc.mouse.y * mc.window.scaledHeight.toDouble() / mc.window.height.toDouble()).toInt()

class LibGuiWidget(private val libGuiWidget: WWidget, width: Int, height: Int) : DevWidget {
    init {
        libGuiWidget.setSize(width, height)
    }

    override val minimumHeight = height
    override val minimumWidth = width

    override fun position(constraints: Constraints) = runtimeWidget(constraints) {
        libGuiWidget.paintBackground(constraints.x, constraints.y)
        libGuiWidget.paintForeground(constraints.x, constraints.y, getClientMouseX(), getClientMouseY())
    }


}


fun DevWidget.runtimeWidget(constraints: Constraints,
                            children: List<RuntimeWidget> = listOf(),
                            debugIdentifier: String = "RuntimeWidget",
                            drawer: RuntimeWidget.() -> Unit) = object : RuntimeWidget {
    override var runtimeChildren = children
    override fun draw() = drawer()
    override val constraints = constraints
    override val composer = this@runtimeWidget
    override val debugIdentifier = debugIdentifier


}

