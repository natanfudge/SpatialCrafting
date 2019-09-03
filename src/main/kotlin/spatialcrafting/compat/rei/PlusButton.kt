package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import me.shedaniel.rei.client.ScreenHelper
import me.shedaniel.rei.gui.widget.ButtonWidget
import me.shedaniel.rei.gui.widget.QueuedTooltip
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Style
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import spatialcrafting.Packets
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.recipe.ComponentSatisfaction
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.recipe.getRecipeSatisfaction
import spatialcrafting.util.*
import java.awt.Point
import java.util.*

private const val width = 10
private const val height = 10


fun fillInRecipeFromPlayerInventory(crafterMultiblock: CrafterMultiblock, recipeId: Identifier) {
    sendPacketToServer(Packets.AutoCraft(crafterMultiblock.arbitraryCrafterPos(), getMinecraftClient().player.uuid, recipeId))
}


fun startCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock, recipeId: Identifier) {
    sendPacketToServer(Packets.StartRecipeHelp(crafterMultiblock.arbitraryCrafterPos(), recipeId))
    Client.scheduleRenderUpdate(crafterMultiblock.arbitraryCrafterPos())
    crafterMultiblock.startRecipeHelpCommon(recipeId)
}

fun stopCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock) {
    sendPacketToServer(Packets.StopRecipeHelp(crafterMultiblock.arbitraryCrafterPos()))
    crafterMultiblock.stopRecipeHelpCommon()
}

fun getNearestCrafter(world: ClientWorld, pos: Vec3d) =
        world.blockEntities.filterIsInstance<CrafterPieceEntity>()
                .minBy { it.pos.distanceFrom(pos) }?.multiblockIn

class PlusButton(x: Int, y: Int, val recipe: SpatialRecipe,
                 private var setLayer: (Int, () -> List<ComponentSatisfaction>?) -> Unit, val display: ReiSpatialCraftingDisplay)
    : ButtonWidget(x, y, width, height, "+") {
    enum class State {
        NO_NEARBY_CRAFTER,
        NEARBY_CRAFTER_TOO_SMALL,
        READY_FOR_RECIPE_HELP,
        ALL_COMPONENTS_AVAILABLE,
        RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
    }

    private var state: State = State.NO_NEARBY_CRAFTER
        set(value) {
            field = value
            this.enabled = value != State.NO_NEARBY_CRAFTER && value != State.NEARBY_CRAFTER_TOO_SMALL
        }

    /**
     * Null if there is no need to display a red highlight
     */
    var recipeSatisfactionForHighlight: List<ComponentSatisfaction>? = null

    override fun getTooltips(): Optional<String> {
        val stringKey = when (state) {
            State.NO_NEARBY_CRAFTER -> "tooltip.rei.plus_button.no_crafter_nearby"
            State.NEARBY_CRAFTER_TOO_SMALL -> "tooltip.rei.plus_button.crafter_too_small"
            State.READY_FOR_RECIPE_HELP -> "tooltip.rei.plus_button.start_recipe_help"
            State.ALL_COMPONENTS_AVAILABLE -> "tooltip.rei.plus_button.all_components_available"
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> "tooltip.rei.plus_button.stop_recipe_help"
        }
        val text = TranslatableText(stringKey)
        if (!this.enabled) text.style = Style().setColor(Formatting.RED)
        return Optional.of(text.asFormattedString())
    }

    override fun onPressed() {
        val pos = minecraft.player.pos
        val world = minecraft.world
        val nearestCrafter = getNearestCrafter(world, pos) ?: return
        when (state) {
            State.READY_FOR_RECIPE_HELP -> startCrafterRecipeHelp(nearestCrafter, recipe.identifier)
            State.ALL_COMPONENTS_AVAILABLE -> fillInRecipeFromPlayerInventory(nearestCrafter, recipe.identifier)
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> stopCrafterRecipeHelp(nearestCrafter)

            else -> error("Impossible")
        }

        minecraft.player.closeScreen()

    }


    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        var shouldHighlightMissingComponents = false
        val pos = minecraft.player.pos
        val world = minecraft.world
        val nearestCrafter = world.blockEntities.filterIsInstance<CrafterPieceEntity>()
                .minBy { it.pos.distanceFrom(pos) }?.multiblockIn
        if (nearestCrafter != null && nearestCrafter.canBeUsedByPlayer(minecraft.player)) {
            if (nearestCrafter.multiblockSize >= recipe.minimumCrafterSize) {

                val (recipeSatisfaction, fullySatisfied) = getRecipeSatisfaction(
                        recipe = recipe,
                        nearestCrafter = nearestCrafter,
                        world = world,
                        player = getMinecraftClient().player
                )

                state = when {
                    fullySatisfied -> State.ALL_COMPONENTS_AVAILABLE
                    nearestCrafter.recipeHelpActive -> State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
                    else -> State.READY_FOR_RECIPE_HELP
                }

                if (state == State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS && this.isHovered(mouseX, mouseY)) {
                    highlightMissingComponents(recipeSatisfaction)
                    shouldHighlightMissingComponents = true
                }


            }
            else {
                state = State.NEARBY_CRAFTER_TOO_SMALL
            }

        }
        else {
            state = State.NO_NEARBY_CRAFTER
        }

        when (state) {
            State.ALL_COMPONENTS_AVAILABLE -> emitGreenColor()
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> emitRedColor()
            else -> {
            }
        }

        if (!shouldHighlightMissingComponents) this.recipeSatisfactionForHighlight = null

        renderLowLevel(mouseX, mouseY, delta)
    }

    private fun highlightMissingComponents(satisfaction: List<ComponentSatisfaction>) {
        this.recipeSatisfactionForHighlight = satisfaction
        setLayer(findFirstUnsatisfiedLayer(satisfaction)) { this.recipeSatisfactionForHighlight }
    }

    private fun findFirstUnsatisfiedLayer(satisfaction: List<ComponentSatisfaction>): Int {
        if (!layerIsSatisfied(display.currentLayer, satisfaction)) return display.currentLayer
        for (i in 0 until display.recipe.minimumCrafterSize) {
            if (!layerIsSatisfied(i, satisfaction)) return i
        }
        error("If this method is called it's impossible that all layers are unsatisfied.")
    }

    private fun layerIsSatisfied(layer: Int, satisfaction: List<ComponentSatisfaction>): Boolean {
        return satisfaction.filter { it.pos.y == layer }.all { it.satisfiedBy != null }
    }


    private fun renderLowLevel(mouseX: Int, mouseY: Int, delta: Float) {
        val x = bounds.x
        val y = bounds.y
        val width = bounds.width
        val height = bounds.height
        minecraft.textureManager.bindTexture(if (ScreenHelper.isDarkModeEnabled()) BUTTON_LOCATION_DARK else BUTTON_LOCATION)
        val textureOffset = getTextureId(isHovered(mouseX, mouseY))
        GlStateManager.enableBlend()
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO)
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
        //Four Corners

        //Four Corners
        //Four Corners
        blit(x, y, 0, textureOffset * 80, 4, 4)
        blit(x + width - 4, y, 252, textureOffset * 80, 4, 4)
        blit(x, y + height - 4, 0, textureOffset * 80 + 76, 4, 4)
        blit(x + width - 4, y + height - 4, 252, textureOffset * 80 + 76, 4, 4)

        //Sides


        //Sides
        //Sides
        blit(x + 4, y, 4, textureOffset * 80, MathHelper.ceil((width - 8) / 2f), 4)
        blit(x + 4, y + height - 4, 4, textureOffset * 80 + 76, MathHelper.ceil((width - 8) / 2f), 4)
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y + height - 4, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80 + 76, MathHelper.floor((width - 8) / 2f), 4)
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80, MathHelper.floor((width - 8) / 2f), 4)


        var i = y + 4
        while (i < y + height - 4) {
            blit(x, i, 0, 4 + textureOffset * 80, MathHelper.ceil(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76))
            blit(x + MathHelper.ceil(width / 2f), i, 256 - MathHelper.floor(width / 2f), 4 + textureOffset * 80, MathHelper.floor(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76))
            i += 76
        }


        var colour = 14737632
        if (!enabled) {
            colour = 10526880
        }
        else if (isHovered(mouseX, mouseY)) {
            colour = 16777120
        }

        drawCenteredString(font, text, x + width / 2, y + (height - 8) / 2, colour)

        if (tooltips.isPresent) if (!focused && isHighlighted(mouseX, mouseY)) ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(*tooltips.get().split("\n").toTypedArray())) else if (focused) ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(Point(x + width / 2, y + height / 2), *tooltips.get().split("\n").toTypedArray()))
    }


    private fun emitRedColor() {
        GlStateManager.color4f(1f, 0f, 0f, 1.0f)
    }

    private fun emitGreenColor() {
        GlStateManager.color4f(0f, 1f, 0f, 1.0f)
    }
}