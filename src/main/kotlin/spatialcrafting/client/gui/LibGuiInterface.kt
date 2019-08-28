package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import spatialcrafting.client.gui.widgets.Clickable
import spatialcrafting.client.gui.widgets.ColumnClass
import spatialcrafting.client.gui.widgets.MainAxisAlignment
import spatialcrafting.util.getMinecraftClient
import spatialcrafting.util.logDebug
import spatialcrafting.util.times

private fun RuntimeWidget.walk(visitor: (RuntimeWidget) -> Unit) {
    visitor(this)
    for (child in runtimeChildren) child.walk(visitor)
}

// Libgui begins the box thing a bit higher
private const val PaddingOffset = 3
//var debugRoot
fun LightweightGuiDescription.drawWidgets(width: Int, height: Int, init: WidgetContext.() -> Unit) {

    val context = ChildrenContext()
    context.init()
    val root: DevWidget = ColumnClass(context.children, MainAxisAlignment.Start)

    val screenWidth = getMinecraftClient().window.scaledWidth
    val screenHeight = getMinecraftClient().window.scaledHeight

    val absoluteX = (screenWidth - width) / 2
    val absoluteY = (screenHeight - height) / 2

    val runtimeRoot: RuntimeWidget = root.position(Constraints(absoluteX, absoluteY /*- PaddingOffset*/, width, height))

    val libGuiRoot = object : WWidget() {
        override fun paintBackground(x: Int, y: Int) {
            runtimeRoot.draw()
//            runtimeRoot.walk { it.draw() }
        }
    }


    rootPanel = object : WPlainPanel() {
        init {
            add(libGuiRoot, 0, 0, width, height)
        }

        override fun onClick(x: Int, y: Int, button: Int) {
            runtimeRoot.walk {
                if (it.constraints.contains(x + absoluteX, y + absoluteY) && it is Clickable.ClickableRuntime) it.onClick()
            }
        }

    }
    rootPanel.setSize(width, height)




    logDebug {
        "Opening screen with widget tree =\n ${runtimeRoot.infoString()}"
    }

}

private fun RuntimeWidget.infoString(nestingLevel: Int = 0): String =
        "  " * nestingLevel + "${this.debugIdentifier}($constraints) " +
                if (runtimeChildren.isNotEmpty()) {
                    "{\n" +
                            runtimeChildren.joinToString("\n") { it.infoString(nestingLevel + 1) } +
                              "\n${("  " * nestingLevel)}}"
                }
                else {
                    ""
                }