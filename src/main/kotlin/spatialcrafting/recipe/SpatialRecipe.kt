@file:UseSerializers(ForItemStack::class, ForIdentifier::class)

package spatialcrafting.recipe

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import drawer.ForIdentifier
import drawer.ForItemStack
import drawer.readFrom
import drawer.write
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import spatialcrafting.MaxCrafterSize
import spatialcrafting.SmallestCrafterSize
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.crafter.sortedByXYZ
import spatialcrafting.util.Duration
import spatialcrafting.util.flatMapIndexed
import spatialcrafting.util.max
import spatialcrafting.util.seconds
import spatialcrafting.util.ticks
import kotlin.math.max

@Serializable
abstract class SpatialRecipe : Recipe<CrafterMultiblockInventoryWrapper> {

    abstract val outputStack: ItemStack
    abstract val identifier: Identifier
    abstract val minimumCrafterSize: Int
    protected abstract val energyCost: Long
    protected abstract val _craftTime: Long

    val craftTime: Duration get() = _craftTime.ticks

    override fun craft(var1: CrafterMultiblockInventoryWrapper): ItemStack = ItemStack.EMPTY

    override fun getId() = identifier

    override fun getType() = Type

    override fun fits(var1: Int, var2: Int) = false

    override fun getOutput() = outputStack

    // For REI
    abstract val previewComponents: List<ShapedRecipeComponent>


    object Type : RecipeType<SpatialRecipe> {
        const val Id = "spatial_crafting"
        override fun toString() = Id
    }

    abstract class Serializer<T : SpatialRecipe> : RecipeSerializer<SpatialRecipe> {
        abstract val serializer: KSerializer<T>

        companion object {
            const val defaultEnergyCost = 1000L
            val defaultCraftTimes = mapOf(
                    1 to 5.seconds,
                    2 to 10.seconds,
                    3 to 15.seconds,
                    4 to 20.seconds,
                    5 to 25.seconds
            )
        }

        override fun write(buf: PacketByteBuf, recipe: SpatialRecipe) = serializer.write(recipe as T, buf)

        override fun read(id: Identifier, buf: PacketByteBuf): SpatialRecipe = serializer.readFrom(buf)

        abstract fun build(components: List<ShapedRecipeComponent>,
                           id: Identifier, output: ItemStack, minimumCrafterSize: Int, energyCost: Long, craftTime: Duration): T

        override fun read(id: Identifier, jsonObject: JsonObject): SpatialRecipe {
            val deserialized = deserializeJson(jsonObject, id)
            validateJson(deserialized, id)
            return readFromDeserialized(deserialized, id)
        }

        fun readFromDeserialized(json: SpatialRecipeJsonFormat, id: Identifier): T {
            validateJson(json, id)
            val ingredients = json.key.mapValues { Ingredient.fromJson(it.value) }
            val components = json.pattern.flatMapIndexed { y, layer ->
                layer.flatMapIndexed { x, row ->
                    row.mapIndexed { z, ingredientKey ->
                        if (ingredientKey == ' ') return@mapIndexed null
                        ShapedRecipeComponent(
                                ComponentPosition(x, y, z),
                                ingredient = ingredients[ingredientKey]
                                        ?: throwNoIngredientWithKeyError(ingredientKey, json.key, id)
                        )
                    }.filterNotNull()
                }
            }.sortedByXYZ()

            if (components.isEmpty()) {
                throw SpatialRecipeSyntaxException("The pattern must not be empty (this pattern is invalid: ${json.pattern} ).", id)
            }

            val outputItem = Registry.ITEM.getOrEmpty(Identifier(json.result.item))
                    .orElseThrow { noItemError(json, id) }


            val output = ItemStack(outputItem, json.result.count ?: 1)

            // The default is that the minimum craft size based on how far spread the components are. If he wants a smaller size
            // If he groups them together he gets a smaller size and if he puts them farther apart he gets a bigger size.
            val recipeSize = max(
                    components.maxBy { it.position.x }!!.position.x,
                    components.maxBy { it.position.y }!!.position.y,
                    components.maxBy { it.position.z }!!.position.z
            ) + 1

            val minimumCrafterSize = json.minimumCrafterSize ?: recipeSize

            val energyCost = json.energyCost ?: defaultEnergyCost
            val craftTime = json.craftTime?.seconds ?: (defaultCraftTimes[minimumCrafterSize]
                    ?: error("impossible crafter size"))

            return build(components, id, output, max(minimumCrafterSize, SmallestCrafterSize), energyCost, craftTime)
        }


         fun deserializeJson(jsonObject: JsonObject, id: Identifier): SpatialRecipeJsonFormat {
            val json: SpatialRecipeJsonFormat
            try {
                json = Gson().fromJson(jsonObject, SpatialRecipeJsonFormat::class.java)
            } catch (e: JsonSyntaxException) {
                throw SpatialRecipeSyntaxException(
                        "The format of the spatial recipe is invalid. Remember that the pattern is an array of arrays (not just an array). More information:\n$e"
                        , id
                )
            }
            return json
        }

        class SpatialRecipeSyntaxException(error: String, recipeId: Identifier) : Exception("Cannot load spatial recipe" +
                " '${recipeId.path}: $error")


        @Suppress("SENSELESS_COMPARISON")
        private fun validateJson(json: SpatialRecipeJsonFormat, id: Identifier) {
            //TODO: validate that energy is enabled when energy is specified
            val missingField = when {
                json.result == null -> "result"
                json.result.item == null -> "result item"
                json.key == null -> "key"
                json.pattern == null -> "pattern"
                else -> null
            }
            if (missingField != null) {
                throw SpatialRecipeSyntaxException("Missing required field '\"$missingField\": { }.'", id)
            }
            if (json.result.count != null && json.result.count < 0) {
                throw SpatialRecipeSyntaxException("The output has an invalid count of '${json.result.count}'.", id)
            }

            if (json.pattern.size > MaxCrafterSize) {
                throw SpatialRecipeSyntaxException("The recipe pattern has too many layers (${json.pattern.size})." +
                        " The maximum amount of layers is $MaxCrafterSize.\n" +
                        "The pattern in question: ${json.pattern}", id)
            }

            for (layer in json.pattern) {
                if (layer.size > MaxCrafterSize) {
                    throw SpatialRecipeSyntaxException("One of the layers in the recipe pattern is too long (length of ${layer.size}." +
                            " The maximum length is $MaxCrafterSize.\n" +
                            "The layer in question: $layer", id)
                }

                for (row in layer) {
                    if (row.length > MaxCrafterSize) {
                        throw SpatialRecipeSyntaxException("One of the rows in the recipe pattern is too long (length of ${row.length})." +
                                " The maximum length is $MaxCrafterSize.\n" +
                                "The row in question: $row", id)
                    }
                }
            }

            if (json.minimumCrafterSize != null) {
                if (json.minimumCrafterSize < 0) {
                    throw SpatialRecipeSyntaxException("minimumCrafterSize cannot be a negative value (${json.minimumCrafterSize}.", id)
                }
                if (json.minimumCrafterSize > MaxCrafterSize) {
                    throw SpatialRecipeSyntaxException("minimunCrafterSize is too big (${json.minimumCrafterSize}," +
                            " as the biggest crafter is of size $MaxCrafterSize.", id)
                }
            }


        }

        private fun throwNoIngredientWithKeyError(ingredientKey: Char, ingredients: Map<Char, JsonObject>, id: Identifier): Nothing {
            val ingredientsString = GsonBuilder().setPrettyPrinting().create().toJson(ingredients)
            throw SpatialRecipeSyntaxException(
                    """ingredient key '$ingredientKey' is not defined. Please define it in the 'key' section. Defined keys:
$ingredientsString""", id)
        }

        private fun noItemError(json: SpatialRecipeJsonFormat, id: Identifier): Throwable {
            return SpatialRecipeSyntaxException("The item defined as the output '${json.result.item}' does not exist.", id)
        }

    }
}



