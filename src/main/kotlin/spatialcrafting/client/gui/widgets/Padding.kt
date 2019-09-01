package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.widgets.core.Overlay
import spatialcrafting.client.gui.widgets.core.SingleChildDevWidget


class PaddingClass(private val left: Int,
                   private val right: Int,
                   private val top: Int,
                   private val bottom: Int,
                   override val composeDirectChildren: DevWidget.() -> Unit, overlay: Overlay?)
    : SingleChildDevWidget(overlay) {
    override val minimumHeight get() = child?.minimumHeight ?: 0 + top + bottom
    override val minimumWidth get() = child?.minimumWidth ?: 0 + left + right

    override fun getLayout(constraints: Constraints) = runtimeWidget(
            constraints, children = if (child != null) {
        listOf(child!!.layout(
                // We nudge the child according to the padding
                Constraints(
                        x = constraints.x + left,
                        y = constraints.y + top,
                        width = constraints.width - right,
                        height = constraints.height - bottom
                )
        ))
    }
    else listOf()
    ) { it.runtimeChildren.firstOrNull()?.draw() }


}

fun DevWidget.Padding(left: Int = 0,
                      right: Int = 0,
                      top: Int = 0,
                      bottom: Int = 0, child: DevWidget.() -> Unit): DevWidget =
        add(PaddingClass(left, right, top, bottom, child,overlay))

