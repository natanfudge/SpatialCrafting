package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.*
import spatialcrafting.client.gui.widgets.MainAxisAlignment.*
import spatialcrafting.util.maxValueBy
import java.lang.Integer.min

enum class MainAxisAlignment {
    Start,
    Center,
    End
}

class ColumnClass(/*private val devChildren: List<DevWidget>, */private val alignment: MainAxisAlignment, override val compose: () -> Unit) : DevWidget {
    override val minimumHeight get() = devChildren.sumBy { it.minimumHeight }
    override val minimumWidth get() = devChildren.maxValueBy { it.minimumWidth } ?: 0
    override val expandHeight = true
    override val expandWidth = false

    private val devChildren = mutableListOf<DevWidget>()

    private fun positionChildren(constraints: Constraints): List<RuntimeWidget> {

        var height = min(constraints.height, devChildren.sumBy { it.minimumHeight })
        val space = constraints.height - height
        val expandingWidgets = devChildren.count { it.expandHeight }
        // Split the space evenly between expanding widgets
        val extraSpaceForExpandingWidgets = when {
            space <= 0 -> 0
            expandingWidgets == 0 -> 0
            else -> space / expandingWidgets
        }
        // The expanding widgets makes this widget take all the space
        if (expandingWidgets >= 1) height = constraints.height

        var currentY = when (alignment) {
            Start -> constraints.y
            Center -> (constraints.y + (constraints.height + constraints.y)) / 2 - height / 2
            End -> constraints.y + constraints.height - height
        }

        val children = mutableListOf<RuntimeWidget>()
        for (widget in devChildren) {
            var widgetHeight = widget.minimumHeight
            if (widget.expandHeight) {
                widgetHeight += extraSpaceForExpandingWidgets
            }
            children.add(widget.position(Constraints(
                    x = constraints.x, y = currentY, width = widget.minimumWidth, height = widgetHeight
            )))

            currentY += widgetHeight
        }

        return children
    }

    override fun position(constraints: Constraints): RuntimeWidget = runtimeWidget(
            constraints = constraints, children = positionChildren(constraints), debugIdentifier = "Column"
    ) {
        for (child in runtimeChildren) child.draw()
    }
}

fun WidgetContext.Column(alignment: MainAxisAlignment = Start, children: () -> Unit): DevWidget =
        add(ColumnClass(alignment, children))

