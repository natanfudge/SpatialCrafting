package spatialcrafting.recipe

import com.google.gson.JsonObject
import spatialcrafting.ModId


const val ShapedRecipeType = "$ModId:shaped"
const val ShapelessRecipeType = "$ModId:shapeless"

data class SpatialRecipeJsonFormat(
        val type: String,
        val pattern: List<List<String>>,
        val key: Map<Char, JsonObject>,
        val result: RecipeResult,
        val minimumCrafterSize: Int?,
        val energyCost: Long?,
        val craftTime: Float?,
        val effect: CraftingEffect?
)

//data class KeyMapping(val tag: String?, val ingredient: String?)

data class RecipeResult(val item: String, val count: Int?)