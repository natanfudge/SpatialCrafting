package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import me.shedaniel.rei.gui.widget.EntryWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GuiLighting
import net.minecraft.item.ItemStack
class HighlightableSlotWidget(val x: Int,val y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
                              showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
                              val highlighted: () -> Boolean,
                              itemCountOverlay: (ItemStack) -> String = { "" })
    : EntryWidget(x, y) {

    init {
        background(drawBackground)
        tooltips(showToolTips)
        interactable(clickToMoreRecipes)

        entries(itemStackList.map { it.reiEntry })


    }
    //TODO: this is pretty broken
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

//    val l = x - 8
//        val i1 = y - 6
//        RenderHelper.color4f(1.0f, 1.0f, 1.0f, 1.0f)
//        val itemRenderer = MinecraftClient.getInstance().itemRenderer
//        itemRenderer.zOffset = blitOffset.toFloat()
//        GuiLighting.enableForItems()
//        RenderHelper.colorMask(true, true, true, true)
//        RenderHelper.enableLighting()
//        RenderHelper.enableRescaleNormal()
//        RenderHelper.enableDepthTest()
//        itemRenderer.renderGuiItem(itemStack, l, i1)
//        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, itemStack, l, i1, itemCountOverlay.invoke(itemStack))
//        itemRenderer.zOffset = 0.0f
//        this.blitOffset = 0

}

//class SlotItemStackRenderer(private val itemStackList: List<ItemStack>, private val itemCountOverlay: (ItemStack) -> String, private val extraTooltips: ((ItemStack) -> List<String>?)?) : ItemStackRenderer() {
//    override fun getItemStack(): ItemStack = if (itemStackList.isEmpty()) ItemStack.EMPTY else itemStackList[(System.currentTimeMillis() / 500 % itemStackList.size.toDouble() / 1f).toInt()]
//    override fun getExtraToolTips(stack: ItemStack): List<String>? {
//        return if (extraTooltips == null) emptyList() else extraTooltips.invoke(stack) ?: emptyList()
//    }
//
//    override fun render(x: Int, y: Int, mouseX: Double, mouseY: Double, delta: Float) {
//
//    }
//}
