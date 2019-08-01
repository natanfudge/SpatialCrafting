package spatialcrafting.recipe

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import spatialcrafting.crafter.CopyableWithPosition
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.crafter.sortedByXYZ
import spatialcrafting.util.assert
import spatialcrafting.util.flatMapIndexed
import spatialcrafting.util.matches

//TODO: add craft time and energy costsw
data class SpatialRecipe(
        private val components: List<RecipeComponent>,
        private val output: ItemStack,
        private val id: Identifier
) : Recipe<CrafterMultiblockInventoryWrapper> {

    override fun craft(var1: CrafterMultiblockInventoryWrapper): ItemStack {
        return ItemStack.EMPTY
    }

    override fun getId() = id

    override fun getType() = Type

    override fun fits(var1: Int, var2: Int): Boolean {
        return false
    }

    override fun getSerializer() = Serializer

    override fun getOutput() = output

    override fun matches(inventoryWrapper: CrafterMultiblockInventoryWrapper, world: World): Boolean {
        if (inventoryWrapper.size != this.components.size) return false
        val inventory = inventoryWrapper.normalizePositions()
        val recipe = components.normalizePositions()

        // Make sure they have been sorted before, as it's a requirement.
        assert { inventory.sortedByXYZ() == inventory && recipe.sortedByXYZ() == recipe }


        return inventory.zip(recipe).all { it.second.ingredient.matches(it.first.itemStack) }

    }

    /**
     * This basically moves the positions to the corner, so no matter how the slots are positioned in space,
     * what matters is the shape. This allows smallers recipes to be crafted on bigger crafters, and generally
     * makes things easier for the player.
     */
    fun <T : CopyableWithPosition<T>> List<CopyableWithPosition<T>>.normalizePositions(): List<T> {
        // Reference positions, to see how much we need to move all of the positions.
        // If a recipe is in the corner already, all of the values will be 0 and there will no movement.
        // But, if a recipe is slightly to the left, it will move it to the right slightly.
        val originX = minBy { it.position.x }!!.position.x
        val originY = minBy { it.position.y }!!.position.y
        val originZ = minBy { it.position.z }!!.position.z
        return map {
            it.copy(
                    ComponentPosition(
                            x = it.position.x - originX, y = it.position.y - originY, z = it.position.z - originZ
                    )
            )
        }
    }


//    fun errorString(ingredientKey :String) = """
//        ingredient key '$ingredientKey' is not defined. Please define it in the 'key' section.
//    """.trimIndent()

    object Type : RecipeType<SpatialRecipe> {
        const val Id = "shaped"
        override fun toString() = Id
    }

    companion object Serializer : RecipeSerializer<SpatialRecipe> {
        override fun write(var1: PacketByteBuf?, var2: SpatialRecipe?) {
            TODO("not implemented")
        }

        override fun read(id: Identifier, jsonObject: JsonObject): SpatialRecipe {
            val json = deserializeJson(jsonObject)

            validateJson(json)
            val ingredients = json.key.mapValues { Ingredient.fromJson(it.value) }
            val components = json.pattern.flatMapIndexed { y, layer ->
                layer.flatMapIndexed { x, row ->
                    row.mapIndexed { z, ingredientKey ->
                        if (ingredientKey == ' ') return@mapIndexed null
                        RecipeComponent(
                                ComponentPosition(x, y, z),
                                ingredient = ingredients[ingredientKey.toString()]
                                        ?: throwNoIngredientWithKeyError(ingredientKey, json.key)
                        )
                    }.filterNotNull()
                }
            }.sortedByXYZ()

            if (components.isEmpty()) {
                throw SpatialRecipeSyntaxException("The pattern must not be empty (this pattern is invalid: ${json.pattern} ).")
            }

            val outputItem = Registry.ITEM.getOrEmpty(Identifier(json.result.item))
                    .orElseThrow { noItemError(json) }


            val output = ItemStack(outputItem, json.result.count ?: 1)


            return SpatialRecipe(
                    components = components,
                    id = id,
                    output = output
            )
        }


        override fun read(var1: Identifier?, var2: PacketByteBuf?): SpatialRecipe {
            TODO("not implemented")
        }

        private fun deserializeJson(jsonObject: JsonObject): SpatialRecipeJsonFormat {
            val json: SpatialRecipeJsonFormat
            try {
                json = Gson().fromJson(jsonObject, SpatialRecipeJsonFormat::class.java)
            } catch (e: JsonSyntaxException) {
                throw SpatialRecipeSyntaxException(
                        "The format of the spatial recipe is invalid. Remember that the pattern is an array of arrays (not just an array). More information:\n$e"
                )
            }
            return json
        }

        class SpatialRecipeSyntaxException(error: String) : Exception("Cannot load spatial recipe: $error")


        @Suppress("SENSELESS_COMPARISON")
        private fun validateJson(json: SpatialRecipeJsonFormat) {
            val missingField = when {
                json.result == null -> "result"
                json.result.item == null -> "result item"
                json.key == null -> "key"
                json.pattern == null -> "pattern"
                else -> null
            }
            if (missingField != null) {
                throw SpatialRecipeSyntaxException("Missing required field '\"$missingField\": { }.'")
            }
            if (json.result.count != null && json.result.count < 0) {
                throw SpatialRecipeSyntaxException("The output has an invalid count of '${json.result.count}'.")
            }

        }

        private fun throwNoIngredientWithKeyError(ingredientKey: Char, ingredients: Map<String, JsonObject>): Nothing {
            val ingredientsString = GsonBuilder().setPrettyPrinting().create().toJson(ingredients)
            throw SpatialRecipeSyntaxException(
                    """ingredient key '$ingredientKey' is not defined. Please define it in the 'key' section. Defined keys:
$ingredientsString""")
        }

        private fun noItemError(json: SpatialRecipeJsonFormat): Throwable {
            return SpatialRecipeSyntaxException("The item defined as the output '${json.result.item}' does not exist.")
        }


    }
}


data class RecipeComponent(override val position: ComponentPosition, val ingredient: Ingredient)
    : CopyableWithPosition<RecipeComponent> {
    override fun copy(newPosition: ComponentPosition) = copy(position = newPosition)


}

// The 'x' 'y' 'z' coordinates of are offset based, meaning they range from 0 to 4, based on how big the multiblock is.
data class ComponentPosition(val x: Int, val y: Int, val z: Int)


