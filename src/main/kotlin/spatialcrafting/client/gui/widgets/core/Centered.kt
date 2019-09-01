//package spatialcrafting.client.gui.widgets.core
//
//import spatialcrafting.client.gui.*
//import spatialcrafting.client.gui.widgets.runtimeWidget
//
////TODO: generalize into Align
//class CenteredClass(overlay: Overlay?, override val composeDirectChildren: DevWidget.() -> Unit) : SingleChildDevWidget(overlay) {
//    override val minimumHeight = child?.minimumHeight ?: 0
//    override val minimumWidth = child?.minimumHeight ?: 0
//
//    override val expandHeight = true
//    override val expandWidth = true
//
//    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints, child?.let {
//        listOf(it.layout(
//                Constraints(
//                        x = constraints.middleX - it.widthIn(constraints) / 2,
//                        y = constraints.middleY - it.heightIn(constraints) / 2,
//                        width = it.widthIn(constraints),
//                        height = it.heightIn(constraints)
//                )
//        ))
//    } ?: listOf()) { it.runtimeChildren.firstOrNull()?.draw() }
//
//
//}
//
//fun DevWidget.Centered(child: DevWidget.() -> Unit) = add(CenteredClass(overlay, child))