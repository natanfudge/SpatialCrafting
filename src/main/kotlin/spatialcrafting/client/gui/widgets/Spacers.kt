package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.*
import spatialcrafting.client.gui.widgets.core.Overlay

class VerticalSpacerClass(height: Int, overlay: Overlay?) : NoChildDevWidget(overlay) {
    override val minimumHeight = height
    override val minimumWidth = 0

    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(constraints) {}
}

fun DevWidget.VerticalSpace(height: Int) : DevWidget = add(VerticalSpacerClass(height,overlay))

class HorizontalSpacerClass(width: Int, overlay: Overlay?) : NoChildDevWidget(overlay) {
    override val minimumHeight = 0
    override val minimumWidth = width

    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(constraints) {}
}

fun DevWidget.HorizontalSpace(width: Int) : DevWidget = add(HorizontalSpacerClass(width,overlay))