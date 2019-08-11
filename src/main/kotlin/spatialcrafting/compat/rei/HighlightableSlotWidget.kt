package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import me.shedaniel.rei.gui.widget.SlotWidget
import net.minecraft.item.ItemStack

//TODO: bring the highlighting more inline with the new REI highlighting
class HighlightableSlotWidget(x: Int, y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
                              showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
                              val itemCountOverlay: (ItemStack) -> String = { "" }, val highlighted: () -> Boolean)
    : SlotWidget(x, y, itemStackList, drawBackground, showToolTips, clickToMoreRecipes) {
    override fun getItemCountOverlay(currentStack: ItemStack): String = itemCountOverlay(currentStack)

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        super.render(mouseX, mouseY, delta)
        if (highlighted()) {
            GlStateManager.disableLighting()
            GlStateManager.disableDepthTest()
            GlStateManager.colorMask(true, true, true, true)
            val color = 0x70A62323
            fillGradient(x, y, x + 16, y + 16, color, color)
            GlStateManager.colorMask(true, true, true, true)
            GlStateManager.enableLighting()
            GlStateManager.enableDepthTest()
        }

    }

}