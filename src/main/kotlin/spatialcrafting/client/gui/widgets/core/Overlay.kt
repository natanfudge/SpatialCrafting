package spatialcrafting.client.gui.widgets.core

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.widgets.NoChildDevWidget
import spatialcrafting.client.gui.widgets.getClientMouseX
import spatialcrafting.client.gui.widgets.getClientMouseY
import spatialcrafting.client.gui.widgets.runtimeWidget
import spatialcrafting.util.getMinecraftClient

class Overlay : NoChildDevWidget(null) {
    override val minimumHeight = 0
    override val minimumWidth = 0
    override val expandHeight = true
    override val expandWidth = true

    var tooltip: String? = null

    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints) {
        if (tooltip != null) {
            val screen = getMinecraftClient().currentScreen
            screen!!.renderTooltip(tooltip, getClientMouseX(), getClientMouseY())
            // Make sure it doesn't linger after
            tooltip = null
        }

    }
}

