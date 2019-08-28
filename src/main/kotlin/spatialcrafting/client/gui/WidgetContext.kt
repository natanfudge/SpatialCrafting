package spatialcrafting.client.gui

import spatialcrafting.client.gui.widgets.Clickable
import spatialcrafting.client.gui.widgets.SingleChildDevWidget

interface WidgetContext {
    fun add(widget: DevWidget): DevWidget
    fun remove(widget: DevWidget)

    fun DevWidget.onClick(callback: RuntimeWidget.() -> Unit): DevWidget {
        remove(this)
        return Clickable(this, callback).also { add(it) }
    }

    fun DevWidget.onHover(callback: RuntimeWidget.() -> Unit): DevWidget {
        remove(this)
        return SingleChildDevWidget(this) {
            draw()
            if (constraints.contains(getClientMouseX(), getClientMouseY())) callback()
        }.also { add(it) }
    }
}

class ChildrenContext : WidgetContext {

    val children: MutableList<DevWidget> = mutableListOf()
    override fun add(widget: DevWidget): DevWidget {
        children.add(widget)
        return widget
    }

    override fun remove(widget: DevWidget) {
        children.remove(widget)
    }
}