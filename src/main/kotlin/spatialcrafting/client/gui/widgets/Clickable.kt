package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget

class Clickable(val onClick: RuntimeWidget.() -> Unit,override val composeDirectChildren: DevWidget.() -> Unit) : DevWidget() {
//    //TODO: might need to change it
//     = {}

    private val child get() = devChildren.first()
    override val minimumHeight get() = child.minimumHeight
    override val minimumWidth get() = child.minimumWidth

    override fun getLayout(constraints: Constraints): RuntimeWidget = ClickableRuntime(constraints, this)
    //
    inner class ClickableRuntime(override val constraints: Constraints, override val origin: DevWidget) : RuntimeWidget {
        override var runtimeChildren = listOf(child.layout(constraints))
        override fun draw() {
            runtimeChildren.first().draw()
        }

        fun onClick() = this@Clickable.onClick(this)

        override val debugIdentifier = "Clickable"

    }
}

