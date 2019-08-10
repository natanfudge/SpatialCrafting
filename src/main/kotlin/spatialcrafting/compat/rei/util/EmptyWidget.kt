package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element

class EmptyWidget : Widget() {
    override fun children(): List<Element> = listOf()

    override fun render(var1: Int, var2: Int, var3: Float) {}
}