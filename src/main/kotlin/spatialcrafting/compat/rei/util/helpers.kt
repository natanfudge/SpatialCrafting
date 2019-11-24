@file:Suppress("FunctionName")

package spatialcrafting.compat.rei.util


import me.shedaniel.rei.gui.widget.EntryWidget
import net.minecraft.item.ItemStack
import spatialcrafting.compat.rei.HighlightableSlotWidget


fun SlotWidget(x: Int, y: Int, itemStack: ItemStack, drawBackground: Boolean = true,
               showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true): EntryWidget {
    return HighlightableSlotWidget(x, y, listOf(itemStack), drawBackground, showToolTips, clickToMoreRecipes, { false })
}

