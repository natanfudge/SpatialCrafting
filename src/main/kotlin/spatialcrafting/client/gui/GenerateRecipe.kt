package spatialcrafting.client.gui

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fabricktx.api.getMinecraftClient
import net.minecraft.item.Item
import net.minecraft.tag.ItemTags
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry
import spatialcrafting.MaxCrafterSize
import spatialcrafting.ModId
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.modId
import spatialcrafting.recipe.*
import java.io.File
import java.nio.file.Paths

private const val GeneratedDataPackName = "${ModId}_generated"

/**
 * Returns the text that will be displayed to the player after generation. Can return errors in player input.
 */
fun generateRecipe(multiblock: CrafterMultiblock, recipeOptions: RecipeOptions): String {
    // If useTags is enabled and an item has multiple tags then we can't resolve the recipe currently.
    //TODO: enable useTags with multiple tags with more advanced GUI.
    if (recipeOptions.useTags) {
        for (slot in multiblock.getInventory(getMinecraftClient().world!!)) {
            if (ItemTags.getContainer().getTagsFor(slot.itemStack.item).size > 1) {
                return errorText("There are multiple tags for '${slot.itemStack.item.name.asFormattedString()}', please create this recipe manually or don't use tags for now.")
            }
        }
    }
    val craftTime = recipeOptions.craftTime.toFloatOrNull() ?: return errorText("'${recipeOptions.craftTime}' is not a valid number.")
    if(craftTime <=0) return errorText("The Craft Time must be a positive number.")
    val minimumCrafterSize = recipeOptions.minimumCrafterSize.toIntOrNull() ?:
    return errorText("'${recipeOptions.minimumCrafterSize}' is not a valid integer.")
    if(minimumCrafterSize < 1 || minimumCrafterSize > MaxCrafterSize) return errorText(
            "The Minimum Crafter Size must be a number from 1 to 5."
    )



    val jsonRecipe = buildJsonRecipeFromInventory(multiblock, recipeOptions,craftTime, minimumCrafterSize)

    val invalidation = checkForRecipeInvalidation(jsonRecipe, shaped = recipeOptions.shaped)
    if (invalidation != null) return invalidation


    val heldItem = getMinecraftClient().player!!.mainHandStack
    val relativePathToRecipes = Paths.get("config", "datapacks", GeneratedDataPackName, "data",
            ModId, "recipes")

    var jsonRelativePath = relativePathToRecipes.resolve("${Registry.ITEM.getId(heldItem.item).path}.json").toString()
    var targetFile = File(getMinecraftClient().runDirectory.toPath().resolve(jsonRelativePath).toString())
    var recipesThatAlreadyExistForItem = 1
    while (targetFile.exists()) {
        recipesThatAlreadyExistForItem++
        jsonRelativePath = relativePathToRecipes.resolve(
                "${Registry.ITEM.getId(heldItem.item).path}_$recipesThatAlreadyExistForItem.json"
        ).toString()
        targetFile = File(getMinecraftClient().runDirectory.toPath().resolve(jsonRelativePath).toString())
    }

    targetFile.parentFile.mkdirs()
    targetFile.writeText(GsonBuilder().setPrettyPrinting().create().toJson(jsonRecipe))

    createMcMetaFileIfNeeded()

    return LiteralText("Recipe saved to $jsonRelativePath!").setStyle(Style().setColor(Formatting.DARK_GREEN)).asFormattedString()


}

private fun createMcMetaFileIfNeeded() {
    val mcMetaFile = File(
            getMinecraftClient().runDirectory.toPath()
                    .resolve(Paths.get("config", "datapacks", GeneratedDataPackName, "pack.mcmeta"))
                    .toString()
    )
    if (!mcMetaFile.exists()) {
        mcMetaFile.writeText("""
{
   "pack": {
      "pack_format": 1,
      "description": "Generated Spatial Crafting Recipes"
   }
}
        """.trimIndent())
    }
}

fun errorText(text: String): String = LiteralText(text).setStyle(Style().setColor(Formatting.RED)).asFormattedString()

private fun checkForRecipeInvalidation(jsonRecipe: SpatialRecipeJsonFormat, shaped: Boolean): String? {
    val parsedRecipeForValidation = if (shaped) {
        ShapedSpatialRecipe.readFromDeserialized(jsonRecipe, modId("for_validation"))
    }
    else {
        ShapelessSpatialRecipe.readFromDeserialized(jsonRecipe, modId("for_validation"))
    }

    val existingRecipeWithSameInput = getMinecraftClient().world!!.recipeManager.values()
            .find {
                if (it is ShapedSpatialRecipe && parsedRecipeForValidation is ShapedSpatialRecipe) {
                    it.components == parsedRecipeForValidation.components
                }
                else if (it is ShapelessSpatialRecipe && parsedRecipeForValidation is ShapelessSpatialRecipe) {
                    it.components == parsedRecipeForValidation.components
                }
                else false
            }
    if (shaped) existingRecipeWithSameInput as? ShapedSpatialRecipe
    else existingRecipeWithSameInput as? ShapelessSpatialRecipe

    if (existingRecipeWithSameInput != null) {
        return if (existingRecipeWithSameInput.output == parsedRecipeForValidation.output) {
            errorText("This recipe already exists.")
        }
        else {
            errorText("There is already a recipe with the same input.")
        }
    }

    return null


}

private fun cubeOfSize(size: Int) = (0 until size)
        .map { 0 until size }
        .map { it.map { 0 until size } }

private  fun List<List<IntRange>>.mapCube(mapping: (ComponentPosition) -> Char) = mapIndexed { y, layer ->
    layer.mapIndexed { x, row ->
        row.map { z -> mapping(ComponentPosition(x, y, z)) }.joinToString("")
    }
}

private fun buildJsonRecipeFromInventory(multiblock: CrafterMultiblock, options: RecipeOptions,
                                         craftTime : Float, minimumCrafterSize : Int): SpatialRecipeJsonFormat {
    val heldItem = getMinecraftClient().player!!.mainHandStack
    val items = multiblock.getInventory(getMinecraftClient().world!!)
    // Map of items in their keys
    val ingredientKeys = mutableMapOf<Item, Char>()
    for (slot in items) {
        val item = slot.itemStack.item
        ingredientKeys[item] = ingredientKeys[item]
                // Item was not encountered before
                ?: item.name.asFormattedString().find { it !in ingredientKeys.values }?.toUpperCase()
                        // Can't use any of the item's letters because they have been used already by other items
                        ?: (0..100).map { it.toChar() }.first { it !in ingredientKeys.values }
    }
    val cube = cubeOfSize(multiblock.multiblockSize)

    val recipeItems = cube.mapCube { cubePosition ->
        items.find { it.position == cubePosition }?.itemStack?.item.let { ingredientKeys[it] }
                ?: ' '
    }

    return SpatialRecipeJsonFormat(
            type = if (options.shaped) ShapedRecipeType else ShapelessRecipeType,
            key = ingredientKeys.map { (item, key) ->
                key to JsonObject().apply {

                    if (options.useTags) {
                        val containing = ItemTags.getContainer().getTagsFor(item)
                        if (containing.isEmpty()) {
                            addProperty("item", Registry.ITEM.getId(item).toString())
                        }
                        else {
                            assert(containing.size == 1) { "We checked before that no item has multiple tags when useTags is on." }
                            addProperty("tag", containing.first().toString())
                        }
                    }
                    else {
                        addProperty("item", Registry.ITEM.getId(item).toString())
                    }

                }
            }.toMap(),
            craftTime = craftTime,
            energyCost = null, //TODO: implement when it's a thing
            minimumCrafterSize = minimumCrafterSize,
            pattern = recipeItems,
            result = RecipeResult(
                    item = Registry.ITEM.getId(heldItem.item).toString(),
                    count = heldItem.count
            ),
            effect = if(options.useItemMovementEffect) CraftingEffect.itemMovement else CraftingEffect.particles
    )
}