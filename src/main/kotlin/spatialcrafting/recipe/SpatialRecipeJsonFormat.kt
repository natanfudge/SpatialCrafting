package spatialcrafting.recipe

import com.google.gson.JsonObject

data class SpatialRecipeJsonFormat(
        val type: String,
        val pattern: List<List<String>>,
        val key: Map<String, JsonObject>,
        val result: Result,
        val minimumCrafterSize: Int?,
        val energyCost: Long?,
        val craftTime: Float?
)

//data class KeyMapping(val tag: String?, val ingredient: String?)

data class Result(val item: String, val count: Int?)