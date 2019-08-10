package spatialcrafting.compat.rei

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import me.shedaniel.rei.client.ScreenHelper
import me.shedaniel.rei.gui.widget.ButtonWidget
import me.shedaniel.rei.gui.widget.QueuedTooltip
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.recipe.*
import spatialcrafting.util.distanceFrom
import spatialcrafting.util.itemsInInventoryAndOffhand
import spatialcrafting.util.matches
import java.awt.Point

const val width = 10
const val height = 10
const val MaxDistanceFromNearestCrafter = 100

//private data class ItemAndAmount(val item: ItemSta)

private fun SpatialRecipe.asShapeless(): ShapelessSpatialRecipe {
    if (this is ShapelessSpatialRecipe) return this
    this as ShapedSpatialRecipe

    val components = this.components.groupBy { it.ingredient }
            .map { (ingredient, componentsThatHaveIngredient) ->
                ShapelessRecipeComponent(ingredient, componentsThatHaveIngredient.size)
            }


    return ShapelessSpatialRecipe(
            components, minimumCrafterSize, energyCost, craftTime, output, id
    )
}


data class ComponentSatisfaction(val pos: ComponentPosition, val satisfiedBy: ItemStack?)
data class RecipeSatisfaction(val componentSatisfaction: List<ComponentSatisfaction>, val fullySatisfied: Boolean)

fun startCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock, recipeId: Identifier) {
    crafterMultiblock.recipeHelpRecipeId = recipeId
}

fun stopCrafterRecipeHelp(crafterMultiblock: CrafterMultiblock) {
    crafterMultiblock.recipeHelpRecipeId = null
}

class PlusButton(x: Int, y: Int, val recipe: SpatialRecipe) : ButtonWidget(x, y, width, height, "+") {
    enum class State {
        INACTIVE,
        READY_FOR_RECIPE_HELP,
        ALL_COMPONENTS_AVAILABLE,
        RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
    }

   private var state: State = State.INACTIVE
        set(value) {
            field = value
            this.enabled = value != State.INACTIVE
        }

    override fun onPressed() {
        val pos = minecraft.player.pos
        val world = minecraft.world
        val nearestCrafter = world.blockEntities.filterIsInstance<CrafterPieceEntity>()
                    .minBy { it.pos.distanceFrom(pos) }?.multiblockIn ?: return
        when(state){
            State.READY_FOR_RECIPE_HELP -> {
                startCrafterRecipeHelp(nearestCrafter, recipe.id)
                minecraft.player.closeScreen()
            }

            State.ALL_COMPONENTS_AVAILABLE -> {
                //TODO
            }
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> {
                stopCrafterRecipeHelp(nearestCrafter)
                minecraft.player.closeScreen()
            }
            else -> error("Impossible")
        }

    }
    //TODO: show missing items on hover

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        val pos = minecraft.player.pos
        val world = minecraft.world
        val nearestCrafter = world.blockEntities.filterIsInstance<CrafterPieceEntity>()
                .minBy { it.pos.distanceFrom(pos) }?.multiblockIn
        if (nearestCrafter != null && nearestCrafter.crafterLocations[0].distanceFrom(pos) <= MaxDistanceFromNearestCrafter) {
            if (nearestCrafter.multiblockSize >= recipe.minimumCrafterSize) {

                state = when {
                    recipeIsCraftableWithCurrentInventory(nearestCrafter, world) -> State.ALL_COMPONENTS_AVAILABLE
                    nearestCrafter.recipeHelpActive -> State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS
                    else -> State.READY_FOR_RECIPE_HELP
                }


            }
            else {
                state = State.INACTIVE

                //TODO emit "crafter is too small tooltip"
            }

        }
        else {
            state = State.INACTIVE
            //TODO: emit "no crafter nearby tooltip"
        }

        when (state) {
            State.ALL_COMPONENTS_AVAILABLE -> {
                emitGreenColor()
                //TODO: emit "click to craft" tooltip
            }
            State.RECIPE_HELP_ACTIVE_WITH_MISSING_COMPONENTS -> {
                emitRedColor()
                //TODO "stop recipe help" tooltip + show what things are missing
            }
            State.READY_FOR_RECIPE_HELP -> {
                //TODO: "Start recipe help" tooltip
            }
            State.INACTIVE ->{}
        }

        renderLowLevel(mouseX, mouseY, delta)
    }

    private fun renderLowLevel(mouseX: Int, mouseY: Int, delta: Float) {
        val x = bounds.x
        val y = bounds.y
        val width = bounds.width
        val height = bounds.height
        minecraft.textureManager.bindTexture(if (ScreenHelper.isDarkModeEnabled()) BUTTON_LOCATION_DARK else BUTTON_LOCATION)
//        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
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

    private fun recipeIsCraftableWithCurrentInventory(nearestCrafter: CrafterMultiblock, world: ClientWorld): Boolean {
        return getRecipeSatisfaction(nearestCrafter, world).fullySatisfied
    }

    private fun getRecipeSatisfaction(nearestCrafter: CrafterMultiblock, world: ClientWorld): RecipeSatisfaction {
        val crafterInventory = nearestCrafter.getInventory(world)

        var fullySatisfied = true
        val playerItems = minecraft.player.itemsInInventoryAndOffhand

        // Tracks how many ingredients an itemstack in the player's inventory satisfies.
        // This is so one itemstack that holds 2 of the item doesn't satisfy 10 different ingredients
        val satisfactionMap = mutableMapOf<ItemStack, Int>()

        val satisfaction = recipe.previewComponents.map { (pos, ingredient) ->
            // Check if the ingredient exists in the multiblock already
            val stackInMultiblockAtPos = crafterInventory.find { it.position == pos }?.itemStack
                    ?: ItemStack.EMPTY

            var satisfiedBy: ItemStack? = null

            if (ingredient.matches(stackInMultiblockAtPos)) {
                satisfiedBy = stackInMultiblockAtPos
            }
            else {
                for (playerStack in playerItems) {
                    if (!ingredient.matches(playerStack)) continue
                    val ingredientsSatisfiedByStack = satisfactionMap[playerStack] ?: 0
                    // Check that a single stack of 2 items does't satisfy more than 2
                    if (playerStack.count > ingredientsSatisfiedByStack) {
                        satisfiedBy = playerStack
                        satisfactionMap[playerStack] = ingredientsSatisfiedByStack + 1
                        break
                    }
                }
            }

            if (satisfiedBy == null) fullySatisfied = false

            return@map ComponentSatisfaction(pos, satisfiedBy)
        }

        return RecipeSatisfaction(satisfaction, fullySatisfied)
    }

    private fun emitRedColor() {
        GlStateManager.color4f(1f, 0f, 0f, 1.0f)
    }

    private fun emitGreenColor() {
        GlStateManager.color4f(0f, 1f, 0f, 1.0f)
    }
}