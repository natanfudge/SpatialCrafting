package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import me.shedaniel.rei.api.*
import me.shedaniel.rei.gui.widget.RecipeBaseWidget
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.id
import spatialcrafting.recipe.ShapedRecipeComponent
import java.awt.Point
import java.awt.Rectangle
import java.util.function.Supplier
import kotlin.math.max

//TODO: show available crafters on the side
//TODO: show current layer number (starting from 1)
//TODO: show Craft time
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


class ReiSpatialCraftingCategory : RecipeCategory<ReiSpatialCraftingDisplay> {


    companion object {
        private fun guiTexture(location: String) = id("textures/gui/$location")

        val Id = id("rei_crafting_category")
        val Backgrounds = mapOf(
                2 to guiTexture("crafter/x2.png"),
                3 to guiTexture("crafter/x3.png"),
                4 to guiTexture("crafter/x4.png"),
                5 to guiTexture("crafter/x5.png")
        )

        val SizesX = mapOf(2 to 98, 3 to 116, 4 to 140, 5 to 158)
        val SizesY = mapOf(2 to 36, 3 to 54, 4 to 72, 5 to 90)

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

        val ButtonsYOffset = mapOf(
                2 to 5,
                3 to 15,
                4 to 23,
                5 to 33
        )

        const val WidthIncrease = 10

    }



    override fun getIdentifier(): Identifier? {
        return Id
    }

    override fun getIcon(): Renderer = Renderable.fromItemStack(ItemStack(CraftersPieces[2]))

    override fun getCategoryName(): String = I18n.translate("category.spatialcrafting.rei")

    override fun setupDisplay(recipeDisplaySupplier: Supplier<ReiSpatialCraftingDisplay>, bounds: Rectangle): List<Widget> {
        return setupDisplay(recipeDisplaySupplier.get(), bounds)
    }

    private fun setupDisplay(display: ReiSpatialCraftingDisplay, bounds: Rectangle): List<Widget> {
        val recipe = display.recipe
        val startPoint = Point(bounds.centerX.toInt() - SizesX.getValue(recipe.minimumCrafterSize) / 2,
                bounds.centerY.toInt() - SizesY.getValue(recipe.minimumCrafterSize) / 2)

        val inputSlots = InputSlotsWidget(currentInputSlots(startPoint,display))

        return listOf(
                background(bounds, startPoint, recipe.minimumCrafterSize),
                inputSlots,
                outputSlot(startPoint, recipe.output, recipe.minimumCrafterSize),
                upButton(startPoint, display,inputSlots),
                downButton(startPoint, display,inputSlots)
        )

    }

    private fun currentInputSlots(startPoint: Point,display: ReiSpatialCraftingDisplay)
            : List<Widget> {
        return inputSlots(startPoint, display.recipe.previewComponents, layer = display.currentLayer)
    }

    private fun upButton(startPoint: Point, display: ReiSpatialCraftingDisplay, inputSlots: InputSlotsWidget): Widget {
        return ReiButton(x = startPoint.x - 10, y = startPoint.y + ButtonsYOffset.getValue(display.recipe.minimumCrafterSize), height = 10, width = 13,
                textureOn = Buttons.UpOn, textureOff = Buttons.UpOff, isEnabled = { display.currentLayer < display.recipe.minimumCrafterSize - 1 }) {
            display.currentLayer++
            // Refresh input to show new layer
            inputSlots.slots = currentInputSlots(startPoint,display)
        }
    }

    private fun downButton(startPoint: Point, display: ReiSpatialCraftingDisplay, inputSlots: InputSlotsWidget): Widget {
        return ReiButton(x = startPoint.x - 10, y = startPoint.y + 15 + ButtonsYOffset.getValue(display.recipe.minimumCrafterSize),
                height = 10, width = 13,
                textureOn = Buttons.DownOn, textureOff = Buttons.DownOff, isEnabled = { display.currentLayer > 0 }) {
            display.currentLayer--
            // Refresh input to show new layer
            inputSlots.slots = currentInputSlots(startPoint,display)
        }
    }

    private fun inputSlots(startPoint: Point, input: List<ShapedRecipeComponent>, layer: Int): List<Widget> {
        //TODO: remove this max when going bellow 0 is impossible
        return input.filter { it.position.y == layer }.map {
            SlotWidget(
                    //TODO: make +10 a constant
                    x = startPoint.x + it.position.x * 18 + 1 + WidthIncrease,
                    y = startPoint.y + it.position.z * 18 + 1,
                    ingredient = it.ingredient
            )
        }
    }

    private fun outputSlot(startPoint: Point, output: ItemStack, minimumSize: Int): Widget {
        return SlotWidget(x = startPoint.x + OutputSlotXOffset.getValue(minimumSize),
                y = startPoint.y + OutputSlotYOffset.getValue(minimumSize),
                itemStack = output, drawBackground = false) {
            when {
                it.count == 1 -> ""
                it.count < 1 -> Formatting.RED.toString() + it.count
                else -> it.count.toString() + ""
            }
        }
    }


    private fun background(bounds: Rectangle, startPoint: Point, minimumSize: Int): Widget {
        return object : RecipeBaseWidget(bounds) {
            override fun render(mouseX: Int, mouseY: Int, delta: Float) {
                super.render(mouseX, mouseY, delta)
                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
                GuiLighting.disable()
                MinecraftClient.getInstance().textureManager.bindTexture(Backgrounds[minimumSize])
                blit(startPoint.x + WidthIncrease, startPoint.y, 0, 0, SizesX.getValue(minimumSize), SizesY.getValue(minimumSize))
            }
        }
    }

    override fun getDisplaySettings(): DisplaySettings<ReiSpatialCraftingDisplay> {
        return object : DisplaySettings<ReiSpatialCraftingDisplay> {
            //TODO: change this once we can control it based on recipe size
            override fun getDisplayHeight(category: RecipeCategory<out RecipeDisplay<*>>): Int {
                return 100
            }

            override fun getMaximumRecipePerPage(category: RecipeCategory<out RecipeDisplay<*>>): Int {
                return 99
            }

            override fun getDisplayWidth(category: RecipeCategory<out RecipeDisplay<*>>, display: ReiSpatialCraftingDisplay): Int {
                category as ReiSpatialCraftingCategory
                val sizesWidth = SizesX.mapValues { it.value + 35 }
                return sizesWidth.getValue(display.recipe.minimumCrafterSize)
            }

        }
    }


}
