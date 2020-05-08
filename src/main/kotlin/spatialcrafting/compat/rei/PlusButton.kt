package spatialcrafting.compat.rei

import fabricktx.api.distanceFrom
import fabricktx.api.getMinecraftClient
import fabricktx.api.scheduleRenderUpdate
import fabricktx.api.sendPacketToServer
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.impl.widgets.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.client.gui.text
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.startRecipeHelpCommon
import spatialcrafting.crafter.stopRecipeHelpCommon
import spatialcrafting.recipe.ComponentSatisfaction
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.recipe.getRecipeSatisfaction


private const val width = 10
private const val height = 10


fun fillInRecipeFromPlayerInventory(crafterMultiblock: CrafterMultiblock, recipeId: Identifier) {
    sendPacketToServer(Packets.AutoCraft(crafterMultiblock.arbitraryCrafterPos, getMinecraftClient().player!!.uuid, recipeId))
}


fun startCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock, recipeId: Identifier) {
    sendPacketToServer(Packets.StartRecipeHelp(crafterMultiblock.arbitraryCrafterPos, recipeId))
    getMinecraftClient().scheduleRenderUpdate(crafterMultiblock.arbitraryCrafterPos)
    crafterMultiblock.startRecipeHelpCommon(recipeId)
}

fun stopCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock) {
    sendPacketToServer(Packets.StopRecipeHelp(crafterMultiblock.arbitraryCrafterPos))
    crafterMultiblock.stopRecipeHelpCommon()
}



fun getNearestCrafter(world: World, pos: Vec3d) =
        world.blockEntities.filterIsInstance<CrafterPieceEntity>()
                .minBy { it.pos.distanceFrom(pos) }?.multiblockIn

class PlusButton(x: Int, y: Int, val recipe: SpatialRecipe,
                 private var setLayer: (Int, () -> List<ComponentSatisfaction>?) -> Unit, val display: ReiSpatialCraftingDisplay)
    : ButtonWidget(Rectangle(x, y, width, height), "+".text) {
    enum class State {
        NO_NEARBY_CRAFTER,
        NEARBY_CRAFTER_TOO_SMALL,
        READY_FOR_RECIPE_HELP,
        ALL_COMPONENTS_AVAILABLE,
        RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
    }

    init {
        setOnClick {
            val pos = minecraft.player!!.pos
            val world = minecraft.world
            val nearestCrafter = getNearestCrafter(world!!, pos) ?: return@setOnClick
            when (state) {
                State.READY_FOR_RECIPE_HELP -> startCrafterRecipeHelp(nearestCrafter, recipe.identifier)
                State.ALL_COMPONENTS_AVAILABLE -> fillInRecipeFromPlayerInventory(nearestCrafter, recipe.identifier)
                State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> stopCrafterRecipeHelp(nearestCrafter)

                else -> error("Impossible")
            }

            minecraft.player!!.closeScreen()
        }

        tooltipSupplier {
            val stringKey = when (state) {
                State.NO_NEARBY_CRAFTER -> "tooltip.rei.plus_button.no_crafter_nearby"
                State.NEARBY_CRAFTER_TOO_SMALL -> "tooltip.rei.plus_button.crafter_too_small"
                State.READY_FOR_RECIPE_HELP -> "tooltip.rei.plus_button.start_recipe_help"
                State.ALL_COMPONENTS_AVAILABLE -> "tooltip.rei.plus_button.all_components_available"
                State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> "tooltip.rei.plus_button.stop_recipe_help"
            }
            var text: MutableText = TranslatableText(stringKey)
            val color = if(this.isEnabled) "" else Formatting.RED.toString()

            return@tooltipSupplier color + text.string
        }

        textColor { button, point ->
            when (state) {
                State.NO_NEARBY_CRAFTER, State.NEARBY_CRAFTER_TOO_SMALL,
                State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> Red

                State.READY_FOR_RECIPE_HELP, State.ALL_COMPONENTS_AVAILABLE -> Green
            }
        }
    }

    private var state: State = State.NO_NEARBY_CRAFTER
        set(value) {
            field = value
            this.isEnabled = value != State.NO_NEARBY_CRAFTER && value != State.NEARBY_CRAFTER_TOO_SMALL
        }

    /**
     * Null if there is no need to display a red highlight
     */
    var recipeSatisfactionForHighlight: List<ComponentSatisfaction>? = null


    override fun render(stack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val minecraft = getMinecraftClient()
        var shouldHighlightMissingComponents = false
        val pos = minecraft.player!!.pos
        val world = minecraft.world
        val nearestCrafter = world!!.blockEntities.filterIsInstance<CrafterPieceEntity>()
                .minBy { it.pos.distanceFrom(pos) }?.multiblockIn
        if (nearestCrafter != null && nearestCrafter.canBeUsedByPlayer(minecraft.player!!)) {
            if (nearestCrafter.multiblockSize >= recipe.minimumCrafterSize) {

                val (recipeSatisfaction, fullySatisfied) = getRecipeSatisfaction(
                        recipe = recipe,
                        nearestCrafter = nearestCrafter,
                        world = world,
                        player = getMinecraftClient().player!!
                )

                state = when {
                    fullySatisfied -> State.ALL_COMPONENTS_AVAILABLE
                    nearestCrafter.recipeHelpActive -> State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
                    else -> State.READY_FOR_RECIPE_HELP
                }

                if (state == State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS && this.isFocused(mouseX, mouseY)) {
                    highlightMissingComponents(recipeSatisfaction)
                    shouldHighlightMissingComponents = true
                }


            } else {
                state = State.NEARBY_CRAFTER_TOO_SMALL
            }

        } else {
            state = State.NO_NEARBY_CRAFTER
        }

        val color: Int? = when (state) {
            State.ALL_COMPONENTS_AVAILABLE -> Green
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> Red
            else -> null
        }

        if (!shouldHighlightMissingComponents) this.recipeSatisfactionForHighlight = null

//        super.render(matrices, mouseX, mouseY, delta)


//        setTint(Red)

        if (color != null) this.setTint(color)

//        enabled(true)

        super.render(stack, mouseX, mouseY, delta)

//        renderLowLevel(stack, mouseX, mouseY, color)
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



}

private const val Red: Int = 0xA0ff0000.toInt()
private const val Green: Int = 0xA000ff00.toInt()