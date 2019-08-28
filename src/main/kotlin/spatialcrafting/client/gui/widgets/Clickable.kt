package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget

//TODO: get rid of "child" params because they are probably not needed
class Clickable(private val child: DevWidget, val onClick: RuntimeWidget.() -> Unit, override val compose: () -> Unit) : DevWidget {
    override val minimumHeight = child.minimumHeight
    override val minimumWidth = child.minimumWidth

    override fun position(constraints: Constraints): RuntimeWidget = ClickableRuntime(constraints, this)
//
    inner class ClickableRuntime(override val constraints: Constraints, override val composer: DevWidget) : RuntimeWidget {
        override var runtimeChildren = listOf(child.position(constraints))
        override fun draw() {
            runtimeChildren.first().draw()
        }

        fun onClick() = this@Clickable.onClick(this)

        override val debugIdentifier = "Clickable"

    }
}

