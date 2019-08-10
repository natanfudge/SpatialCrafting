package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import me.shedaniel.rei.api.*
import me.shedaniel.rei.gui.widget.LabelWidget
import me.shedaniel.rei.gui.widget.RecipeBaseWidget
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GuiLighting
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
import spatialcrafting.id
import spatialcrafting.util.drawCenteredStringWithoutShadow
import spatialcrafting.util.isWholeNumber
import java.awt.Point
import java.awt.Rectangle
import java.util.function.Supplier
import kotlin.math.roundToInt

//TODO: better example recipes
//TODO: show energy cost if that's enabled (and exists)
//TODO: One plus button:
// Queries the combined items of the nearest crafter and the player's inventory.
// While crafter help is inactivate:
//     - No nearby crafter - button is disabled
//     - There is nearby crafter but player doesn't have enough items - button is enabled, and clicking begins crafter help.
//     - There is nearby crafter and player has enough items - button is green, and clicking fills all of the items in.
// While crafter help is active:
//     - No nearby crafter - button is disabled
//     - There is nearby crafter but player + crafter don't have enough items - button is red, and clicking ends crafter help.
//           - While hovering, it highlights in red the missing items in the current layer.
//           - If no items are missing in the current layer, it jumps to a layer that does have missing items.
//     - There is nearby crafter and player + crafter have enough items - Button is green and clicking fills in the missing items.
// Every state has an appropriate tooltip!


class ReiSpatialCraftingCategory(val recipeSize: Int) : RecipeCategory<ReiSpatialCraftingDisplay> {
    private fun <V> Map<Int, V>.ofRecipeSize() = getValue(recipeSize)

    companion object {
        private fun guiTexture(location: String) = id("textures/gui/$location")


        fun id(size: Int) = id("rei_crafting_category_x$size")
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

            val PlusOff = guiTexture("button/plus_off.png")
            val PlusOn = guiTexture("button/plus_on.png")
            val PlusRed = guiTexture("button/plus_red.png")
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

    override fun getIcon(): Renderer = Renderable.fromItemStack(ItemStack(CraftersPieces[recipeSize]))

    override fun getCategoryName(): String = I18n.translate("category.rei.spatialcrafting.x$recipeSize")

    override fun setupDisplay(recipeDisplaySupplier: Supplier<ReiSpatialCraftingDisplay>, bounds: Rectangle): List<Widget> {
        return setupDisplay(recipeDisplaySupplier.get(), bounds)
    }

    private fun setupDisplay(display: ReiSpatialCraftingDisplay, bounds: Rectangle): List<Widget> {
        val startPoint = Point(bounds.centerX.toInt() - RecipeWidth.ofRecipeSize() / 2,
                bounds.centerY.toInt() - RecipeHeight.ofRecipeSize() / 2)
        return DisplayFactory(
                startPoint = startPoint,
                display = display, bounds = bounds,
                // We use swappable widgets and set their data later because we need them to change when the player does stuff.
                inputSlots = SwappableChildrenWidget(),
                recipeSize = recipeSize,
                currentLayerWidget = SwappableChildWidget()
        ).getWidgets()

    }

    private class DisplayFactory(
            val display: ReiSpatialCraftingDisplay,
            val startPoint: Point,
            val bounds: Rectangle,
            val inputSlots: SwappableChildrenWidget,
            val currentLayerWidget: SwappableChildWidget,
            val recipeSize: Int
    ) {
        private fun <V> Map<Int, V>.ofRecipeSize() = getValue(recipeSize)

        fun getWidgets(): List<Widget> {
            refreshLayerWidgets()

            return listOf(
                    background(),
                    inputSlots,
                    outputSlot(display.recipe.output),
                    upButton(),
                    downButton(),
                    currentLayerWidget,
                    craftTimeText(),
                    plusButton()
//                    availableCrafters()
            )
        }

        private fun plusButton() : Widget{
            val xOffset = 19
            val yOffset = if(recipeSize == 2) 19 else 4
            return PlusButton(
                    x = bounds.x + RecipeWidth.ofRecipeSize() + xOffset,
                    y = bounds.y + RecipeHeight.ofRecipeSize() + yOffset,
                    recipe = display.recipe
            )
        }


        private fun currentLayerText(): Widget {
            val text = LiteralText((display.currentLayer + 1).toString())
                    .setStyle(Style().setColor(Formatting.AQUA))

            return LabelWidget(
                    startPoint.x + UpDownButtonsXOffset + 6,
                    startPoint.y + UpDownButtonsYOffset.ofRecipeSize() - 11,
                    text.asFormattedString()
            )
        }

        private fun craftTimeText(): Widget {
            val seconds = display.recipe.craftTime.inSeconds
            val time = if (seconds.isWholeNumber()) seconds.roundToInt() else seconds
            val text = LiteralText(time.toString() + "s")
                    .setStyle(Style().setColor(Formatting.DARK_GRAY))



            return object : LabelWidget(
                    startPoint.x + CraftTimeXOffset.ofRecipeSize(),
                    startPoint.y + OutputSlotYOffset.ofRecipeSize() + 20,
                    text.asFormattedString()
            ) {
                override fun render(mouseX: Int, mouseY: Int, delta: Float) {
                    drawCenteredStringWithoutShadow(font, this.text, x, y, -1)
                }
            }
        }

        private fun refreshLayerWidgets() {
            // Refresh inputs slots
            inputSlots.children = inputSlots()
            currentLayerWidget.child = currentLayerText()
        }


        private fun upButton(): Widget {
            return ReiButton(x = startPoint.x + UpDownButtonsXOffset,
                    y = startPoint.y + UpDownButtonsYOffset.ofRecipeSize(), height = 10, width = 13,
                    textureOn = Buttons.UpOn, textureOff = Buttons.UpOff,
                    isEnabled = { display.currentLayer < recipeSize - 1 }) {
                display.currentLayer++

                refreshLayerWidgets()
            }
        }

        private fun downButton(): Widget {
            return ReiButton(x = startPoint.x + UpDownButtonsXOffset,
                    y = startPoint.y + 15 + UpDownButtonsYOffset.ofRecipeSize(),
                    height = 10, width = 13,
                    textureOn = Buttons.DownOn, textureOff = Buttons.DownOff, isEnabled = { display.currentLayer > 0 }) {
                display.currentLayer--

                refreshLayerWidgets()
            }
        }

        private fun inputSlots(): List<Widget> {
            return display.recipe.previewComponents.filter { it.position.y == display.currentLayer }.map {
                SlotWidget(
                        x = startPoint.x + it.position.x * 18 + 1 + WidthIncrease,
                        y = startPoint.y + it.position.z * 18 + 1,
                        ingredient = it.ingredient
                )
            }
        }

        private fun outputSlot(output: ItemStack): Widget {
            return SlotWidget(x = startPoint.x + OutputSlotXOffset.ofRecipeSize(),
                    y = startPoint.y + OutputSlotYOffset.ofRecipeSize(),
                    itemStack = output, drawBackground = false) {
                when {
                    it.count == 1 -> ""
                    it.count < 1 -> Formatting.RED.toString() + it.count
                    else -> it.count.toString() + ""
                }
            }
        }


        private fun background(): Widget {
            return object : RecipeBaseWidget(bounds) {
                override fun render(mouseX: Int, mouseY: Int, delta: Float) {
                    super.render(mouseX, mouseY, delta)
                    GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
                    GuiLighting.disable()
                    MinecraftClient.getInstance().textureManager.bindTexture(Background.ofRecipeSize())
                    blit(startPoint.x + WidthIncrease, startPoint.y, 0, 0, RecipeWidth.ofRecipeSize(),
                            RecipeHeight.ofRecipeSize())
                }
            }
        }
    }


    override fun getDisplaySettings(): DisplaySettings<ReiSpatialCraftingDisplay> {
        return object : DisplaySettings<ReiSpatialCraftingDisplay> {
            //TODO: change this once we can control it based on recipe size
            override fun getDisplayHeight(category: RecipeCategory<out RecipeDisplay<*>>): Int {
                if (recipeSize == 2) return RecipeHeight.ofRecipeSize() + 35
                return RecipeHeight.ofRecipeSize() + 20
            }

            override fun getMaximumRecipePerPage(category: RecipeCategory<out RecipeDisplay<*>>): Int {
                return 99
            }

            override fun getDisplayWidth(category: RecipeCategory<out RecipeDisplay<*>>, display: ReiSpatialCraftingDisplay): Int {
//                if(recipeSize == 2) return RecipeWidth.ofRecipeSize() + 70
                return RecipeWidth.ofRecipeSize() + 35
            }

        }
    }


}
