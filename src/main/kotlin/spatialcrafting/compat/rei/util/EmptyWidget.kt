package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element
import net.minecraft.client.util.math.MatrixStack

class EmptyWidget : Widget() {
    override fun children(): List<Element> = emptyList()

    override fun render(stack: MatrixStack, var1: Int, var2: Int, var3: Float) {}
}