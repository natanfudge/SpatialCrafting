package spatialcrafting.compat.rei

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Point
import me.shedaniel.rei.api.REIHelper
import me.shedaniel.rei.gui.widget.EntryWidget
import me.shedaniel.rei.server.ContainerInfo
import me.shedaniel.rei.server.ContainerInfoHandler
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.math.MathHelper

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

    override fun render(stack:MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(stack,mouseX, mouseY, delta)

        if (highlighted()) {
            stack.push()
            stack.translate(0.0, 0.0, 400.0)
            DrawableHelper.fill(stack, x, y, x + 16, y + 16, 0x60ff0000)
            stack.pop()
        }

    }

}

