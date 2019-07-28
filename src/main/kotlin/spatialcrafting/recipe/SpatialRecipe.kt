package spatialcrafting.recipe

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.util.flatMapIndexed


//TODO: test various recipes and response
//TODO: make error messages more informative
//TODO: measure speed of reflection vs manual with JMH
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
        val inventory = inventoryWrapper.inventory
        //TODO
        return false
    }

//    fun errorString(ingredientKey :String) = """
//        ingredient key '$ingredientKey' is not defined. Please define it in the 'key' section.
//    """.trimIndent()

    object Type : RecipeType<SpatialRecipe> {
        const val Id = "spatial_crafting"
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
                        RecipeComponent(
                                ComponentPosition(x, y, z),
                                ingredient = ingredients[ingredientKey.toString()]
                                        ?: throwNoIngredientWithKeyError(ingredientKey, json.key)
                        )
                    }
                }
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
                throw JsonSyntaxException(recipeLoadingError(
                        "The format of the spatial recipe is invalid. Remember that the pattern is an array of arrays (not just an array). More information:\n$e"
                ))
            }
            return json
        }

        private fun recipeLoadingError(error: String) = "Cannot load spatial recipe: $error"


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
                throw JsonSyntaxException(recipeLoadingError("Missing required field '\"$missingField\": { }'"))
            }
            if (json.result.count != null && json.result.count < 0) {
                throw JsonSyntaxException(recipeLoadingError("The output has an invalid count of '${json.result.count}'."))
            }
        }

        private fun throwNoIngredientWithKeyError(ingredientKey: Char, ingredients: Map<String, JsonObject>): Nothing {
            val ingredientsString = GsonBuilder().setPrettyPrinting().create().toJson(ingredients)
            throw JsonSyntaxException(
                    recipeLoadingError(
                            """ingredient key '$ingredientKey' is not defined. Please define it in the 'key' section. Defined keys:
$ingredientsString"""))
        }

        private fun noItemError(json: SpatialRecipeJsonFormat): Throwable {
            return JsonSyntaxException(recipeLoadingError("The item defined as the output '${json.result.item}' does not exist."))
        }


    }
}


data class RecipeComponent(val position: ComponentPosition, val ingredient: Ingredient)
// The 'x' 'y' 'z' coordinates of are offset based, meaning they range from 0 to 4, based on how big the multiblock is.
data class ComponentPosition(val x: Int, val y: Int, val z: Int)


