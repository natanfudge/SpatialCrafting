package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.runtimeWidget

abstract class OptionalSingleChildDevWidget : DevWidget() {
    protected val child get() = devChildren.firstOrNull()
}

open class TightSingleChildDevWidget(
        override val composeDirectChildren : DevWidget.() -> Unit,
        private val drawer: (RuntimeWidget) -> Unit) : DevWidget() {
    private val child get() = devChildren.first()
    override val minimumHeight get() = child.minimumHeight
    override val minimumWidth get() = child.minimumWidth
    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints,
            listOf(child.layout(constraints))) {
        drawer(it.runtimeChildren.first())
    }




}

//abstract class SingleChildDevWidget(
//        private val drawer: RuntimeWidget.() -> Unit) : DevWidget() {
//    private val child get() = devChildren.first()
//    override val minimumHeight get() = child.minimumHeight
//    override val minimumWidth get() = child.minimumWidth
//    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints,
//            listOf(child.layout(constraints))) {
//        drawer(this.runtimeChildren.first())
//    }
//
//    override val compose = child.compose
//
//
//}