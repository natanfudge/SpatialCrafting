package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.*

class VerticalSpacerClass(height: Int) : DevWidget() {
    override val minimumHeight = height
    override val minimumWidth = 0

    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(constraints) {}
}

fun DevWidget.VerticalSpace(height: Int) : DevWidget = add(VerticalSpacerClass(height))

class HorizontalSpacerClass(width: Int) : DevWidget() {
    override val minimumHeight = 0
    override val minimumWidth = width

    override fun getLayout(constraints: Constraints): RuntimeWidget = runtimeWidget(constraints) {}
}

fun DevWidget.HorizontalSpace(width: Int) : DevWidget = add(VerticalSpacerClass(width))