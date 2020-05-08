package spatialcrafting.client.gui.widgets.core

import net.minecraft.client.util.math.MatrixStack
import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget

class Clickable<T : DevWidget>(overlay: Overlay?,
                val onClick: T.(RuntimeWidget) -> Unit,
                override val composeDirectChildren: DevWidget.() -> Unit) : DevWidget(overlay) {

    private val child get() = devChildren.first()
    override val minimumHeight get() = child.minimumHeight
    override val minimumWidth get() = child.minimumWidth

    override fun getLayout(constraints: Constraints): RuntimeWidget = ClickableRuntime(constraints, this)
    inner class ClickableRuntime(override val constraints: Constraints, override val origin: DevWidget) : RuntimeWidget {
        override var runtimeChildren = listOf(child.layout(constraints))
        override fun draw(stack: MatrixStack) {
            runtimeChildren.first().draw(stack)
        }

        fun onClick() = try {
            val callback = this@Clickable.onClick
            @Suppress("UNCHECKED_CAST")
            (child as T).callback(this)
        } catch (e: Exception) {
            println("ERROR: unable to process click.")
            e.printStackTrace()
        }

        override val debugIdentifier = "Clickable"

    }
}

