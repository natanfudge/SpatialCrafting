package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import spatialcrafting.client.gui.widgets.core.Clickable
import spatialcrafting.client.gui.widgets.core.ColumnClass
import spatialcrafting.client.gui.widgets.core.Overlay
import spatialcrafting.client.gui.widgets.core.Stack
import spatialcrafting.util.getMinecraftClient
import spatialcrafting.util.logDebug
import spatialcrafting.util.times

private fun RuntimeWidget.walk(visitor: (RuntimeWidget) -> Unit) {
    visitor(this)
    for (child in runtimeChildren) child.walk(visitor)
}

fun DevWidget.walk(visitor: (DevWidget) -> Unit) {
    visitor(this)
    for (child in devChildren) child.walk(visitor)
}



//var debugRoot
fun LightweightGuiDescription.drawWidgets(width: Int, height: Int, init: DevWidget.() -> Unit) {

    val overlay = Overlay()
    val root: DevWidget = ColumnClass(overlay = overlay) {
        Stack {
            init()
            add(overlay)
        }
    }
    root.walk { it.composeDirectChildren(it) }

    val screenWidth = getMinecraftClient().window.scaledWidth
    val screenHeight = getMinecraftClient().window.scaledHeight

    val absoluteX = (screenWidth - width) / 2
    val absoluteY = (screenHeight - height) / 2

    val runtimeRoot: RuntimeWidget = root.layout(Constraints(absoluteX, absoluteY , width, height))

    val libGuiRoot = object : WWidget() {
        override fun paintBackground(x: Int, y: Int) {
            runtimeRoot.draw()
        }
    }


    rootPanel = object : WPlainPanel() {
        init {
            add(libGuiRoot, 0, 0, width, height)
        }

        override fun onClick(x: Int, y: Int, button: Int) {
            runtimeRoot.walk {
                if (it.constraints.contains(x + absoluteX, y + absoluteY) && it is Clickable<*>.ClickableRuntime) it.onClick()
            }
        }

    }
    rootPanel.setSize(width, height)

    logDebug {
        "Opening screen with widget tree =\n ${runtimeRoot.infoString()}"
    }

}

fun RuntimeWidget.infoString(nestingLevel: Int = 0): String =
        "  " * nestingLevel + "${this.debugIdentifier}($constraints) " +
                if (runtimeChildren.isNotEmpty()) {
                    "{\n" +
                            runtimeChildren.joinToString("\n") { it.infoString(nestingLevel + 1) } +
                            "\n${("  " * nestingLevel)}}"
                }
                else {
                    ""
                }