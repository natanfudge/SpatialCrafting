package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element

class SwappableChildWidget(var child: Widget = EmptyWidget()) : Widget() {

    override fun children(): List<Element> = listOf(child)

    override fun render(var1: Int, var2: Int, var3: Float) {
        child.render(var1,var2, var3)
    }

}