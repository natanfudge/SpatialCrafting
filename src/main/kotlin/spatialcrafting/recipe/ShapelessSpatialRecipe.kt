@file:UseSerializers(ForIngredient::class, ForItemStack::class, ForIdentifier::class)

package spatialcrafting.recipe

import drawer.ForIdentifier
import drawer.ForIngredient
import drawer.ForItemStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.world.World
import spatialcrafting.client.Duration
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.util.matches

@Serializable
class ShapelessSpatialRecipe private constructor(private val components: List<ShapelessRecipeComponent>,
                             override val minimumCrafterSize: Int,
                             override val energyCost: Long,
                             override val _craftTime: Long,
                             override val outputStack: ItemStack,
                             override val identifier: Identifier) : SpatialRecipe() {

    constructor(components: List<ShapelessRecipeComponent>,
                minimumCrafterSize: Int,
                energyCost: Long,
                craftTime: Duration,
                outputStack: ItemStack,
                identifier: Identifier,
                workaround : Byte = 0.toByte()
    ) : this(components, minimumCrafterSize, energyCost, craftTime.inTicks, outputStack, identifier)

    override val previewComponents: List<ShapedRecipeComponent>
        get() {
            val input = mutableListOf<ShapedRecipeComponent>()
            var componentsIndex = 0
            // Just shove all of them into the grid one by one
            for (x in 0 until minimumCrafterSize) {
                for (y in 0 until minimumCrafterSize) {
                    for (z in 0 until minimumCrafterSize) {
                        input.add(ShapedRecipeComponent(
                                position = ComponentPosition(x, y, z), ingredient = components[componentsIndex].ingredient
                        ))
                        componentsIndex++
                        if (componentsIndex == input.size) return input // We're done adding everything
                    }
                }
            }
            return input
        }

    override fun getSerializer() = Serializer

    data class ItemAndAmount(val stack: ItemStack, val amount: Int)

    override fun matches(inventory: CrafterMultiblockInventoryWrapper, world: World): Boolean {
        val shapelessInventory = inventory.groupBy { it.itemStack.item }.map { ItemAndAmount(it.value[0].itemStack, it.value.size) }
        if (shapelessInventory.size != components.size) return false
        if (minimumCrafterSize > inventory.crafterSize) return false

        return components.all { component ->
            // Check every component exists in the inventory with the required amount
            shapelessInventory.any { component.ingredient.matches(it.stack) && component.amount == it.amount }
        }

    }

    companion object Serializer : SpatialRecipe.Serializer<ShapelessSpatialRecipe>() {
        override val serializer: KSerializer<ShapelessSpatialRecipe>
            get() = serializer()

        override fun build(components: List<ShapedRecipeComponent>, id: Identifier, output: ItemStack,
                           minimumCrafterSize: Int, energyCost: Long, craftTime: Duration): ShapelessSpatialRecipe {

            // We only care about the ingredients and the amount of each one. Position has no meaning.
            val shapelessComponents = components.groupBy { it.ingredient.ids }
                    .map { ShapelessRecipeComponent(it.value[0].ingredient, it.value.size) }

            return ShapelessSpatialRecipe(
                    components = shapelessComponents,
                    minimumCrafterSize = minimumCrafterSize,
                    outputStack = output,
                    identifier = id,
                    energyCost = energyCost,
                    craftTime = craftTime
            )
        }


    }

}

@Serializable
data class ShapelessRecipeComponent(val ingredient: Ingredient, val amount: Int)