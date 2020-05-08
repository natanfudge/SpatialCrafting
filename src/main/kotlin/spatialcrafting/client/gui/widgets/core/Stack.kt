package spatialcrafting.client.gui.widgets.core

import net.minecraft.client.util.math.MatrixStack
import spatialcrafting.client.gui.*
import spatialcrafting.client.gui.widgets.runtimeWidget
import spatialcrafting.maxValueBy

class StackClass(override val composeDirectChildren: DevWidget.() -> Unit, overlay: Overlay?) : DevWidget(overlay) {
    override val minimumHeight get() = devChildren.maxValueBy { it.minimumHeight } ?: 0
    override val minimumWidth get() = devChildren.maxValueBy { it.minimumWidth } ?: 0
    override val expandHeight = true
    override val expandWidth = true


    private fun positionChildren(constraints: Constraints): List<RuntimeWidget> = devChildren.map {
        it.layout(Constraints(
                x = constraints.x,
                y = constraints.y,
                width = it.widthIn(constraints),
                height = it.heightIn(constraints)
        ))
    }

    override fun getLayout(constraints: Constraints) = runtimeWidget(
            constraints, positionChildren(constraints), "Stack"
    ) {drawExt(it)}

    private fun RuntimeWidget.drawExt(stack : MatrixStack) {
        for (child in runtimeChildren) {
            // Notably will draw the first child at the bottom and the last child at the top
            child.draw(stack)
        }
    }
}

fun DevWidget.Stack(children: DevWidget.() -> Unit) :DevWidget = add(StackClass(children,overlay))