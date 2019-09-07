package spatialcrafting.crafter

import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.world.World
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.getHologramEntity
import spatialcrafting.recipe.ShapedRecipeComponent
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.isServer
import spatialcrafting.util.logDebug
import spatialcrafting.util.matches




fun CrafterMultiblock.bumpRecipeHelpCurrentLayerIfNeeded(world: World) {
    if (layerIsComplete(recipeHelpCurrentLayer, world)) {
        if (recipeHelpCurrentLayer < multiblockSize - 1) {
            logDebug { "Bumping recipe help layer" }
            recipeHelpCurrentLayer++
            hideAndShowHologramsForRecipeHelp(world)
            bumpRecipeHelpCurrentLayerIfNeeded(world)
        }
        else {
            showHologramsWithItemOnly(world)
            logDebug { "Recipe help done" }
        }

    }
}

fun CrafterMultiblock.startRecipeHelpServer(recipeId: Identifier, world: World) {
    assert(world.isServer)
    startRecipeHelpCommon(recipeId)
    bumpRecipeHelpCurrentLayerIfNeeded(world)
    hideAndShowHologramsForRecipeHelp(world)
}

fun CrafterMultiblock.startRecipeHelpCommon(recipeId: Identifier) {
    recipeHelpRecipeId = recipeId
    recipeHelpCurrentLayer = 0
}


fun CrafterMultiblock.stopRecipeHelpServer(world: World) {
    assert(world.isServer)
    stopRecipeHelpCommon()
    showAllHolograms(world)
}

fun CrafterMultiblock.stopRecipeHelpCommon() {
    recipeHelpRecipeId = null
}



fun CrafterMultiblock.hologramGhostIngredientFor(hologram: HologramBlockEntity): Ingredient? {
    val components = helpRecipeComponents(hologram.world!!) ?: return null
    val relativePos = hologram.pos.relativeTo(originHologramPos())
    return components.find { it.position == relativePos }?.ingredient
}

private fun CrafterMultiblock.hideAndShowHologramsForRecipeHelp(world: World) {
    val recipeInputs = helpRecipeComponents(world)!!
    for (hologram in hologramsNotOfLayer(recipeHelpCurrentLayer)) {
        setHologramVisibility(world, hologram.absolutePos, hidden = true)
    }


    for (hologram in hologramsOfLayer(recipeHelpCurrentLayer)) {
        val ingredient = recipeInputs.find { it.position == hologram.relativePos }
        if (ingredient != null) {
            setHologramVisibility(world, hologram.absolutePos, hidden = false)
        }
        else {
            setHologramVisibility(world, hologram.absolutePos, hidden = true)
        }
    }

}


private fun CrafterMultiblock.showAllHolograms(world: World) {
    for (pos in hologramLocations) {
        setHologramVisibility(world, pos, hidden = false)
    }
}

private fun CrafterMultiblock.helpRecipeComponents(world: World): List<ShapedRecipeComponent>? = recipeHelpRecipeId?.let {
    (world.recipeManager.get(it).orElse(null)!! as SpatialRecipe).previewComponents
}


private fun CrafterMultiblock.layerIsComplete(layer: Int, world: World): Boolean {
    val holograms = hologramsRelativePositions()
    return helpRecipeComponents(world)!!.filter { it.position.y == layer }
            .all { component ->
                component.ingredient.matches(
                        world.getHologramEntity(holograms.first { it.relativePos == component.position }.absolutePos)
                                .getItem()
                )
            }
}