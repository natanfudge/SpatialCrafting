package spatialcrafting.client.gui.widgets.core

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.widgets.runtimeWidget

abstract class SingleChildDevWidget(overlay: Overlay?) : DevWidget(overlay) {
    protected val child get() = devChildren.firstOrNull()

    fun singleChildRuntimeWidget(constraints: Constraints, debugIdentifier: String = "SingleChildWidget",
                                 childProducer: (child: DevWidget) -> RuntimeWidget,
                                 drawer: (child: RuntimeWidget) -> Unit) = runtimeWidget(
            constraints = constraints, debugIdentifier = debugIdentifier,
            children = child?.let { listOf(childProducer(it)) } ?: listOf(),
            drawer = {it.runtimeChildren.firstOrNull()?.let(drawer)}
    )
}

open class TightSingleChildDevWidget(
        override val composeDirectChildren: DevWidget.() -> Unit,
        private val drawer: (RuntimeWidget) -> Unit,
        overlay: Overlay?
) : DevWidget(overlay) {
    private val child get() = devChildren.first()
    override val minimumHeight get() = child.minimumHeight
    override val minimumWidth get() = child.minimumWidth
    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints,
            listOf(child.layout(constraints))) {
        drawer(it.runtimeChildren.first())
    }


}
