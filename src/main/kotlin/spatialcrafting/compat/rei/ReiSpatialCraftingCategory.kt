package spatialcrafting.compat.rei

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.api.Point
import me.shedaniel.math.api.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeCategory
import me.shedaniel.rei.gui.widget.LabelWidget
import me.shedaniel.rei.gui.widget.RecipeBaseWidget
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import spatialcrafting.compat.rei.util.SlotWidget
import spatialcrafting.compat.rei.util.SwappableChildWidget
import spatialcrafting.compat.rei.util.SwappableChildrenWidget
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.modId
import spatialcrafting.recipe.ComponentSatisfaction
import spatialcrafting.util.isWholeNumber
import java.util.function.Supplier
import kotlin.math.roundToInt

//TODO: show energy cost if that's enabled (and exists)


class ReiSpatialCraftingCategory(private val recipeSize: Int) : RecipeCategory<ReiSpatialCraftingDisplay> {
    private fun <V> Map<Int, V>.ofRecipeSize() = getValue(recipeSize)

    companion object {
        private fun guiTexture(location: String) = modId("textures/gui/$location")


        fun id(size: Int) = modId("rei_crafting_category_x$size")
        val Background = mapOf(
                2 to guiTexture("crafter/x2.png"),
                3 to guiTexture("crafter/x3.png"),
                4 to guiTexture("crafter/x4.png"),
                5 to guiTexture("crafter/x5.png")
        )


        val RecipeWidth = mapOf(2 to 98, 3 to 116, 4 to 140, 5 to 158)
        val RecipeHeight = mapOf(2 to 36, 3 to 54, 4 to 72, 5 to 90)

        object Buttons {
            val DownOff = guiTexture("button/down_off.png")
            val DownOn = guiTexture("button/down_on.png")
            val UpOff = guiTexture("button/up_off.png")
            val UpOn = guiTexture("button/up_on.png")
        }


        val OutputSlotYOffset = mapOf(
                2 to 10,
                3 to 18,
                4 to 28,
                5 to 37
        )
        val OutputSlotXOffset = mapOf(
                2 to 87,
                3 to 105,
                4 to 129,
                5 to 147
        )

        val UpDownButtonsYOffset = mapOf(
                2 to 5,
                3 to 15,
                4 to 23,
                5 to 33
        )

        val CraftTimeXOffset = mapOf(
                2 to 64,
                3 to 82,
                4 to 104,
                5 to 122
        )


        const val WidthIncrease = 10

        const val UpDownButtonsXOffset = -10

    }


    override fun getIdentifier(): Identifier? {
        return id(recipeSize)
    }

    override fun getLogo(): EntryStack = EntryStack.create(ItemStack(CraftersPieces[recipeSize]))

    override fun getCategoryName(): String = I18n.translate("category.rei.spatialcrafting.x$recipeSize")

    override fun setupDisplay(recipeDisplaySupplier: Supplier<ReiSpatialCraftingDisplay>, bounds: Rectangle): List<Widget> {
        return try {
            setupDisplay(recipeDisplaySupplier.get(), bounds)
        } catch (e: NoClassDefFoundError) {
            e.printStackTrace()
            listOf()
        }
    }

    private fun setupDisplay(display: ReiSpatialCraftingDisplay, bounds: Rectangle): List<Widget> {
        val startPoint = Point(bounds.centerX - RecipeWidth.ofRecipeSize() / 2,
                bounds.centerY - RecipeHeight.ofRecipeSize() / 2)

        val inputSlots = SwappableChildrenWidget()
        val currentLayerWidget = SwappableChildWidget()

        val setLayer = { layer: Int, satisfaction: () -> List<ComponentSatisfaction>? ->
            if (layer != display.currentLayer) {
                display.currentLayer = layer
                refreshLayerWidgets(inputSlots, display, startPoint, satisfaction, currentLayerWidget)
            }

        }

        val plusButton = run {
            val xOffset = 19
            val yOffset = if (recipeSize == 2) 19 else 4
            return@run PlusButton(
                    x = bounds.x + RecipeWidth.ofRecipeSize() + xOffset,
                    y = bounds.y + RecipeHeight.ofRecipeSize() + yOffset,
                    recipe = display.recipe,
                    setLayer = setLayer,
                    display = display
            )
        }

        val seconds = display.recipe.craftTime.inSeconds
        val time = if (seconds.isWholeNumber()) seconds.roundToInt().toDouble() else seconds
        val text = LiteralText(time.toString() + "s")
                .setStyle(Style().setColor(Formatting.DARK_GRAY))


        val craftTimeText = LabelWidget(
                Point(startPoint.x + CraftTimeXOffset.ofRecipeSize(), startPoint.y + OutputSlotYOffset.ofRecipeSize() + 20),
                text.asFormattedString()
        ).noShadow()
//        {
//            override fun render(mouseX: Int, mouseY: Int, delta: Float) {
//                Client.drawCenteredStringWithoutShadow(font, this.text, x, y, -1)
//            }
//        }

        fun refreshLayerWidgets() {
            // Refresh inputs slots
            inputSlots.children = inputSlots(display, startPoint) { plusButton.recipeSatisfactionForHighlight }
            currentLayerWidget.child = currentLayerText(display, startPoint)
        }


        val upButton = ReiButton(x = startPoint.x + UpDownButtonsXOffset,
                y = startPoint.y + UpDownButtonsYOffset.ofRecipeSize(), height = 10, width = 13,
                textureOn = Buttons.UpOn, textureOff = Buttons.UpOff,
                isEnabled = { display.currentLayer < recipeSize - 1 }) {
            display.currentLayer++

            refreshLayerWidgets()
        }

        val downButton = ReiButton(x = startPoint.x + UpDownButtonsXOffset,
                y = startPoint.y + 15 + UpDownButtonsYOffset.ofRecipeSize(),
                height = 10, width = 13,
                textureOn = Buttons.DownOn, textureOff = Buttons.DownOff, isEnabled = { display.currentLayer > 0 }) {
            display.currentLayer--

            refreshLayerWidgets()
        }


        val outputSlot = SlotWidget(x = startPoint.x + OutputSlotXOffset.ofRecipeSize(),
                y = startPoint.y + OutputSlotYOffset.ofRecipeSize(),
                itemStack = display.recipe.outputStack, drawBackground = false)


        val background = object : RecipeBaseWidget(bounds) {
            override fun render(mouseX: Int, mouseY: Int, delta: Float) {
                super.render(mouseX, mouseY, delta)
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
                DiffuseLighting.disable()
                MinecraftClient.getInstance().textureManager.bindTexture(Background.ofRecipeSize())
                blit(startPoint.x + WidthIncrease, startPoint.y, 0, 0, RecipeWidth.ofRecipeSize(),
                        RecipeHeight.ofRecipeSize())
            }
        }


        refreshLayerWidgets()

        return listOf(
                background,
                inputSlots,
                outputSlot,
                upButton,
                downButton,
                currentLayerWidget,
                craftTimeText,
                plusButton
        )

    }

    private fun refreshLayerWidgets(inputSlots: SwappableChildrenWidget,
                                    display: ReiSpatialCraftingDisplay, startPoint: Point, satisfaction: () -> List<ComponentSatisfaction>?,
                                    currentLayerWidget: SwappableChildWidget) {
        // Refresh inputs slots
        inputSlots.children = inputSlots(display, startPoint, satisfaction)
        currentLayerWidget.child = currentLayerText(display, startPoint)

    }

    private fun currentLayerText(display: ReiSpatialCraftingDisplay, startPoint: Point): LabelWidget {
        val text = LiteralText((display.currentLayer + 1).toString())
                .setStyle(Style().setColor(Formatting.AQUA))

        return LabelWidget(
                Point(startPoint.x + UpDownButtonsXOffset + 6, startPoint.y + UpDownButtonsYOffset.ofRecipeSize() - 11),
                text.asFormattedString()
        )

    }

    private fun inputSlots(display: ReiSpatialCraftingDisplay, startPoint: Point, satisfaction: () -> List<ComponentSatisfaction>?): List<Widget> {
        return display.recipe.previewComponents.filter { it.position.y == display.currentLayer }.map { component ->
            HighlightableSlotWidget(
                    x = startPoint.x + component.position.x * 18 + 1 + WidthIncrease,
                    y = startPoint.y + component.position.z * 18 + 1,
                    itemStackList = component.ingredient.matchingStacksClient.toList(),
                    highlighted = {
                        val satisfactionResult = satisfaction()
                        if (satisfactionResult == null) false
                        else satisfactionResult.find { it.pos == component.position }!!.satisfiedBy == null
                    }
            )
        }
    }

    override fun getDisplayHeight(): Int {
        if (recipeSize == 2) return RecipeHeight.ofRecipeSize() + 35
        return RecipeHeight.ofRecipeSize() + 20
    }

    override fun getDisplayWidth(display: ReiSpatialCraftingDisplay?): Int = RecipeWidth.ofRecipeSize() + 35

}
