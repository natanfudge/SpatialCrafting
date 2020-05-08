package spatialcrafting.client.gui.widgets.core

import net.minecraft.client.util.math.MatrixStack
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.widgets.core.MainAxisAlignment.Start
import spatialcrafting.client.gui.widgets.runtimeWidget
import spatialcrafting.maxValueBy


class ColumnClass(mainAxisAlignment: MainAxisAlignment = Start,
                  mainAxisSize : FlexSize = FlexSize.Expand,
                  crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
                  private val crossAxisSize: FlexSize = FlexSize.Wrap,
                  overlay: Overlay?, override val composeDirectChildren: DevWidget.() -> Unit)
    : Flex(mainAxisAlignment, crossAxisAlignment,mainAxisSize, overlay) {
    override val minimumHeight get() = devChildren.sumBy { it.minimumHeight }
    override val minimumWidth get() = devChildren.maxValueBy { it.minimumWidth } ?: 0
    override val expandHeight = mainAxisSize == FlexSize.Expand
    override val expandWidth get() = crossAxisSize == FlexSize.Expand || devChildren.any { it.expandWidth }


    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(
            constraints = constraints, children = positionFlexLayout(constraints, direction = Direction.TopToBottom),
            debugIdentifier = "Column", drawer = {drawExt(it)}
    )

    private fun RuntimeWidget.drawExt(stack : MatrixStack) {
        for (child in runtimeChildren) child.draw(stack)
    }

}


fun DevWidget.Column(mainAxisAlignment: MainAxisAlignment = Start,
                     mainAxisSize: FlexSize = FlexSize.Expand,
                     crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
                     crossAxisSize: FlexSize = FlexSize.Wrap,
                     children: DevWidget.() -> Unit): DevWidget =
        add(ColumnClass(mainAxisAlignment, mainAxisSize,crossAxisAlignment,crossAxisSize, overlay,children))

