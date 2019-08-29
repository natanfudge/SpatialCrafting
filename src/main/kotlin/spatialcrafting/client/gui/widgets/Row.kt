package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.runtimeWidget
import spatialcrafting.client.gui.widgets.Direction.LeftToRight
import spatialcrafting.client.gui.widgets.MainAxisAlignment.*
import spatialcrafting.util.maxValueBy


enum class Direction {
    LeftToRight, TopToBottom
}

class RowClass(mainAxisAlignment: MainAxisAlignment,
crossAxisAlignment: CrossAxisAlignment,
               override val composeDirectChildren: DevWidget.() -> Unit) : Flex(mainAxisAlignment,crossAxisAlignment) {
    override val minimumHeight get() = devChildren.maxValueBy { it.minimumHeight } ?: 0
    override val minimumWidth get() = devChildren.sumBy { it.minimumHeight }
    override val expandHeight get() = devChildren.any { it.expandHeight }
    override val expandWidth = true


    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(
            constraints = constraints, children = positionFlexLayout(constraints, direction = LeftToRight), debugIdentifier = "Row"
    ) {
        for (child in it.runtimeChildren) child.draw()
    }
}

fun DevWidget.Row(mainAxisAlignment: MainAxisAlignment = Start,
                  crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
                  children: DevWidget.() -> Unit): DevWidget =
        add(RowClass(mainAxisAlignment,crossAxisAlignment, children))

