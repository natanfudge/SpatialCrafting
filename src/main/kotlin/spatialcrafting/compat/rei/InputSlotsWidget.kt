package spatialcrafting.compat.rei

import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.Element

class InputSlotsWidget( var slots: List<Widget>) : Widget() {

    override fun children(): List<Element> = slots

    override fun render(var1: Int, var2: Int, var3: Float) {
        for (slot in slots) slot.render(var1, var2, var3)
    }

}