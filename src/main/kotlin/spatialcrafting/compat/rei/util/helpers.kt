@file:Suppress("FunctionName")

package spatialcrafting.compat.rei.util

import me.shedaniel.rei.gui.widget.SlotWidget
import net.minecraft.item.ItemStack
import spatialcrafting.compat.rei.HighlightableSlotWidget


fun SlotWidget(x: Int, y: Int, itemStack: ItemStack, drawBackground: Boolean = true,
               showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
               itemCountOverlay: (ItemStack) -> String = { "" }): SlotWidget {
    return SlotWidget(x, y, listOf(itemStack), drawBackground, showToolTips, clickToMoreRecipes, itemCountOverlay)
}

fun SlotWidget(x: Int, y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
               showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
               itemCountOverlay: (ItemStack) -> String = { "" }): SlotWidget {
    return HighlightableSlotWidget(x, y, itemStackList, drawBackground, showToolTips, clickToMoreRecipes, { false }, itemCountOverlay)
}
