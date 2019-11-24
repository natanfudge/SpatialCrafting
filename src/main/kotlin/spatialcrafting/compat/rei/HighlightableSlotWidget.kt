package spatialcrafting.compat.rei

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.rei.gui.widget.EntryWidget
import net.minecraft.item.ItemStack

class HighlightableSlotWidget(val x: Int, val y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
                              showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
                              val highlighted: () -> Boolean)
    : EntryWidget(x, y) {

    init {
        background(drawBackground)
        tooltips(showToolTips)
        interactable(clickToMoreRecipes)

        entries(itemStackList.map { it.reiEntry })


    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        super.render(mouseX, mouseY, delta)
        if (highlighted()) {
            RenderSystem.disableLighting()
            RenderSystem.disableDepthTest()
            RenderSystem.colorMask(true, true, true, true)
            val color = 0x70A62323
            fillGradient(x, y, x + 16, y + 16, color, color)
            RenderSystem.colorMask(true, true, true, true)
            RenderSystem.enableLighting()
            RenderSystem.enableDepthTest()
        }

    }

}

