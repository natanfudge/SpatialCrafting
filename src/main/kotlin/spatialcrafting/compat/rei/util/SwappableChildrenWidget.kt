package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element
import net.minecraft.client.util.math.MatrixStack

class SwappableChildrenWidget(var children: List<Widget> = listOf()) : Widget() {

    override fun children(): List<Element> = children

    override fun render(stack: MatrixStack, var1: Int, var2: Int, var3: Float) {
        for (slot in children) slot.render(stack,var1, var2, var3)
    }

}