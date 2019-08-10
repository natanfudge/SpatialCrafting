package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element

class SwappableChildrenWidget(var children: List<Widget> = listOf()) : Widget() {

    override fun children(): List<Element> = children

    override fun render(var1: Int, var2: Int, var3: Float) {
        for (slot in children) slot.render(var1, var2, var3)
    }

}