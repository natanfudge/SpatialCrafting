package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.*
import spatialcrafting.client.gui.widgets.MainAxisAlignment.*
import spatialcrafting.util.maxValueBy


class ColumnClass(mainAxisAlignment: MainAxisAlignment,
                  crossAxisAlignment: CrossAxisAlignment,
                  override val composeDirectChildren: DevWidget.() -> Unit) : Flex(mainAxisAlignment,crossAxisAlignment) {
    override val minimumHeight get() = devChildren.sumBy { it.minimumHeight }
    override val minimumWidth get() = devChildren.maxValueBy { it.minimumWidth } ?: 0
    override val expandHeight = true
    override val expandWidth get() = devChildren.any { it.expandWidth }


    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(
            constraints = constraints, children = positionFlexLayout(constraints, direction = Direction.TopToBottom), debugIdentifier = "Column"
    ) {
        for (child in it.runtimeChildren) child.draw()
    }
}


fun DevWidget.Column(mainAxisAlignment: MainAxisAlignment = Start,
                     crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
                     children: DevWidget.() -> Unit): DevWidget =
        add(ColumnClass(mainAxisAlignment, crossAxisAlignment,children))

