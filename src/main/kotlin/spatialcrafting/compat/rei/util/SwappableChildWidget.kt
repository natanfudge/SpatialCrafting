package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element
import net.minecraft.client.util.math.MatrixStack

class SwappableChildWidget(var child: Widget = EmptyWidget()) : Widget() {

    override fun children(): List<Element> = listOf(child)

    override fun render(stack: MatrixStack, var1: Int, var2: Int, var3: Float) {
        child.render(stack,var1,var2, var3)
    }

}