package spatialcrafting.client.gui.widgets.core

import fabricktx.api.getMinecraftClient
import net.minecraft.text.Text
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.widgets.NoChildDevWidget
import spatialcrafting.client.gui.widgets.getClientMouseX
import spatialcrafting.client.gui.widgets.getClientMouseY
import spatialcrafting.client.gui.widgets.runtimeWidget

class Overlay : NoChildDevWidget(null) {
    override val minimumHeight = 0
    override val minimumWidth = 0
    override val expandHeight = true
    override val expandWidth = true



    var tooltip: Text? = null

    override fun getLayout(constraints: Constraints) = runtimeWidget(constraints) {stack ->
        if (tooltip != null) {
            val screen = getMinecraftClient().currentScreen
            screen!!.renderTooltip(stack,tooltip, getClientMouseX(), getClientMouseY())
            // Make sure it doesn't linger after
            tooltip = null
        }

    }
}

