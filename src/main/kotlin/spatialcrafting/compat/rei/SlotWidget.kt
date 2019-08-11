///*
// * Roughly Enough Items by Danielshe.
// * Licensed under the MIT License.
// */
//
//package spatialcrafting.compat.rei
//
//import com.google.common.collect.Lists
//import com.mojang.blaze3d.platform.GlStateManager
//import me.shedaniel.cloth.api.ClientUtils
//import me.shedaniel.rei.api.ClientHelper
//import me.shedaniel.rei.api.Renderable
//import me.shedaniel.rei.api.Renderer
//import me.shedaniel.rei.client.ScreenHelper
//import me.shedaniel.rei.gui.renderables.ItemStackRenderer
//import me.shedaniel.rei.gui.widget.HighlightableWidget
//import me.shedaniel.rei.gui.widget.ItemListOverlay
//import me.shedaniel.rei.gui.widget.QueuedTooltip
//import net.minecraft.client.gui.Element
//import net.minecraft.item.ItemStack
//import net.minecraft.util.Identifier
//import net.minecraft.util.math.MathHelper
//import java.awt.Rectangle
//import java.util.*
//import java.util.function.Predicate
//import java.util.stream.Collectors
////x: Int, y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
////                              showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
////                              val itemCountOverlay: (ItemStack) -> String = { "" }, val highlighted: () -> Boolean
//class SlotWidget(var x: Int, var y: Int, val itemList: List<ItemStack>, renderers: List<Renderer>,
//                 var isDrawBackground: Boolean, var isShowToolTips: Boolean, val itemCountOverlay: (ItemStack) -> String
//, val highlighted: () -> Boolean
//)
//    : HighlightableWidget() {
//    private var renderers: List<Renderer> = LinkedList()
//    var isClickToMoreRecipes: Boolean
//    var isDrawHighlightedBackground: Boolean
//
//    constructor(x: Int, y: Int, itemList: Collection<ItemStack>, drawBackground: Boolean, showToolTips: Boolean)
//            : this(x, y, itemList.stream().map { stack: ItemStack -> Renderable.fromItemStack(stack) }
//            .collect(Collectors.toList()), drawBackground, showToolTips) {}
//    constructor(x: Int, y: Int, itemList: List<ItemStack>, drawBackground: Boolean, showToolTips: Boolean,
//                clickToMoreRecipes: Boolean) :
//            this(x, y, itemList, drawBackground, showToolTips) {
//        isClickToMoreRecipes = clickToMoreRecipes
//    }
//
//    constructor(x: Int, y: Int, itemStackList: List<ItemStack>, drawBackground: Boolean = true,
//                              showToolTips: Boolean = true, clickToMoreRecipes: Boolean = true,
//                               itemCountOverlay: (ItemStack) -> String = { "" },  highlighted: () -> Boolean)
//    :this()
//
//    override fun children(): List<Element>? {
//        return emptyList()
//    }
//
//    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
//        val renderer = currentRenderer
//        val darkTheme = ScreenHelper.isDarkModeEnabled()
//        if (isDrawBackground) {
//            minecraft.textureManager.bindTexture(if (darkTheme) RECIPE_GUI_DARK else RECIPE_GUI)
//            blit(x - 1, y - 1, 0, 222, 18, 18)
//        }
//        val highlighted = isHighlighted(mouseX, mouseY)
//        if (isDrawHighlightedBackground && highlighted) {
//            GlStateManager.disableLighting()
//            GlStateManager.disableDepthTest()
//            GlStateManager.colorMask(true, true, true, false)
//            val color = if (darkTheme) -0xa1a1a2 else -2130706433
//            fillGradient(x, y, x + 16, y + 16, color, color)
//            GlStateManager.colorMask(true, true, true, true)
//            GlStateManager.enableLighting()
//            GlStateManager.enableDepthTest()
//        }
//        if (isCurrentRendererItem && !currentItemStack!!.isEmpty) {
//            renderer!!.blitOffset = 200
//            renderer.render(x + 8, y + 6, mouseX.toDouble(), mouseY.toDouble(), delta)
//            if (!currentItemStack!!.isEmpty && highlighted && isShowToolTips) queueTooltip(currentItemStack, delta)
//        }
//        else {
//            renderer!!.blitOffset = 200
//            renderer.render(x + 8, y + 6, mouseX.toDouble(), mouseY.toDouble(), delta)
//        }
//    }
//
//    var blitOffset: Int = 0
//
//    private fun queueTooltip(itemStack: ItemStack?, delta: Float) {
//        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack!!)))
//    }
//
//    private fun getTooltip(itemStack: ItemStack): List<String> {
//        val modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.item)
//        val toolTip: MutableList<String> = Lists.newArrayList(ItemListOverlay.tryGetItemStackToolTip(itemStack, true))
//        val s1 = ClientHelper.getInstance().getModFromItem(itemStack.item).toLowerCase(Locale.ROOT)
//        toolTip.addAll(getExtraToolTips(itemStack))
//        if (!modString!!.isEmpty()) {
//            toolTip.removeIf{ s: String -> s.toLowerCase(Locale.ROOT).contains(s1) }
//            toolTip.add(modString)
//        }
//        return toolTip
//    }
//
//    private fun getExtraToolTips(stack: ItemStack): List<String> {
//        return emptyList()
//    }
//
//    private fun getItemCountOverlay(currentStack: ItemStack?): String {
//        return ""
//    }
//
//    val currentItemStack: ItemStack?
//        get() = if (currentRenderer is ItemStackRenderer) (currentRenderer as ItemStackRenderer?)!!.itemStack else ItemStack.EMPTY
//
//    val currentRenderer: Renderer?
//        get() = if (renderers.size == 0) Renderable.empty() else renderers[MathHelper.floor(System.currentTimeMillis() / 500 % renderers.size.toDouble() / 1f)]
//
//    fun setItemList(itemList: List<ItemStack>) {
//        setRenderers(itemList.stream().map { stack: ItemStack -> Renderable.fromItemStack(stack) }.collect(Collectors.toList()))
//    }
//
//    fun setRenderers(renderers: List<Renderer>) {
//        this.renderers = renderers
//    }
//
//    override fun getBounds(): Rectangle? {
//        return Rectangle(x - 1, y - 1, 18, 18)
//    }
//
//    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        if (!isClickToMoreRecipes) return false
//        if (isCurrentRendererItem && bounds!!.contains(mouseX, mouseY)) if (button == 0) return ClientHelper.getInstance().executeRecipeKeyBind(currentItemStack) else if (button == 1) return ClientHelper.getInstance().executeUsageKeyBind(currentItemStack)
//        return false
//    }
//
//    val isCurrentRendererItem: Boolean
//        get() = currentRenderer is ItemStackRenderer
//
//    override fun keyPressed(int_1: Int, int_2: Int, int_3: Int): Boolean {
//        if (!isClickToMoreRecipes) return false
//        if (isCurrentRendererItem && bounds!!.contains(ClientUtils.getMouseLocation())) if (ClientHelper.getInstance().recipeKeyBinding.matchesKey(int_1, int_2)) return ClientHelper.getInstance().executeRecipeKeyBind(currentItemStack) else if (ClientHelper.getInstance().usageKeyBinding.matchesKey(int_1, int_2)) return ClientHelper.getInstance().executeUsageKeyBind(currentItemStack)
//        return false
//    }
//
//    companion object {
//        val RECIPE_GUI = Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png")
//        val RECIPE_GUI_DARK = Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png")
//    }
//
//    init {
//        this.renderers = renderers
//        isClickToMoreRecipes = false
//        isDrawHighlightedBackground = true
//    }
//}