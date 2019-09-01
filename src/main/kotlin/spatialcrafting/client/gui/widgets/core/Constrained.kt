package spatialcrafting.client.gui.widgets.core

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget

 class ConstrainedClass(overlay: Overlay?, override val composeDirectChildren: DevWidget.() -> Unit) : SingleChildDevWidget(overlay) {
    override fun getLayout(constraints: Constraints) = singleChildRuntimeWidget(
            constraints = constraints, debugIdentifier = "Constrained", childProducer = {
        it.layout(constraints)
    }, drawer = { it.draw() }
    )

    override val minimumHeight get() = child?.minimumHeight ?: 0
    override val minimumWidth get() = child?.minimumWidth ?: 0

}

fun DevWidget.Constrained(child: DevWidget.() -> Unit) = add(ConstrainedClass(overlay,child))