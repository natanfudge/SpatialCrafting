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
 //TODO: show available crafters on the side
//TODO: up and down buttons (extend HighlightableWidget), remember to disable if you can no longer go up/down
//TODO: show Craft time
//TODO: show energy cost if that's enabled (and exists)
//TODO: Two buttons:
// 1 - Does crafting help with the ghost blocks. Shows error tooltip for when there is no nearby crafter.
// Is toggled off when crafting help is already active.
// 2 - Only after release - Sends all of the material in nearby crafter if you have them.
// Otherwise it will show a tooltip of what is missing


class ReiSpatialCraftingCategory : RecipeCategory<ReiSpatialCraftingDisplay> {


    companion object {
        val Id = id("rei_crafting_category")
        val Backgrounds = mapOf(
                2 to id("textures/gui/crafter/x2.png"),
                3 to id("textures/gui/crafter/x3.png"),
                4 to id("textures/gui/crafter/x4.png"),
                5 to id("textures/gui/crafter/x5.png")
        )

        val SizesX = mapOf(2 to 98, 3 to 116, 4 to 140, 5 to 158)
        val SizesY = mapOf(2 to 36, 3 to 54, 4 to 72, 5 to 90)

        val OutputSlotYOffset = mapOf(
                2 to 10,
                3 to 18,
                4 to 28,
                5 to 37
        )
        val OutputSlotXOffset = mapOf(
                2 to 77,
                3 to 95,
                4 to 119,
                5 to 137
        )

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

        return listOf(
                background(bounds, startPoint, recipe.minimumCrafterSize),
                *inputSlots(startPoint, recipe.previewComponents).toTypedArray(),
                outputSlot(startPoint, recipe.output, recipe.minimumCrafterSize),
                upButton(startPoint)
        )

    }

    private fun upButton(startPoint: Point): Widget {
        return ButtonWidget(x = startPoint.x - 15, y = startPoint.y, height = 10, width = 15) {

        }
    }

    private fun downButton(startPoint: Point): Widget {
        return ButtonWidget(x = startPoint.x - 15, y = startPoint.y + 10, height = 10, width = 10) {

        }
    }

    private fun inputSlots(startPoint: Point, input: List<ShapedRecipeComponent>): List<Widget> {
        return input.filter { it.position.y == 0 }.map {
            SlotWidget(
                    x = startPoint.x + it.position.x * 18 + 1,
                    y = startPoint.y + it.position.z * 18 + 1,
                    ingredient = it.ingredient
            )
        }
    }

    private fun outputSlot(startPoint: Point, output: ItemStack, minimumSize: Int): Widget {

        return SlotWidget(x = startPoint.x + OutputSlotXOffset.getValue(minimumSize), y = startPoint.y + OutputSlotYOffset.getValue(minimumSize),
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
                blit(startPoint.x, startPoint.y, 0, 0, SizesX.getValue(minimumSize), SizesY.getValue(minimumSize))
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
