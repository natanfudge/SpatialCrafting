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
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.util.Duration
import spatialcrafting.util.matches
import kotlin.properties.Delegates

@Serializable
class ShapelessSpatialRecipe private constructor( val components: List<ShapelessRecipeComponent>,
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
                workaround: Byte = 0.toByte()
    ) : this(components, minimumCrafterSize, energyCost, craftTime.inTicks, outputStack, identifier)

    override val previewComponents: List<ShapedRecipeComponent> by lazy {
        val input = mutableListOf<ShapedRecipeComponent>()

        var x = 0
        var y = 0
        var z = 0
        for (component in components) {
            for (instanceOfStack in 0 until component.amount) {
                input.add(ShapedRecipeComponent(
                        position = ComponentPosition(x, y, z), ingredient = component.ingredient
                ))

                x++
                if (x == minimumCrafterSize) {
                    x = 0
                    z++
                }
                if (z == minimumCrafterSize) {
                    z = 0
                    y++
                }
            }

        }

        return@lazy input
    }
    
    override fun getSerializer() = Serializer

    data class ItemAndAmount(val stack: ItemStack, val amount: Int, var wasUsed : Boolean)


    //TODO: won't work sometimes
    override fun matches(inventory: CrafterMultiblockInventoryWrapper, world: World): Boolean {
        if (minimumCrafterSize > inventory.crafterSize) return false
        val shapelessInventory = inventory.groupBy { it.itemStack.item }
                .map { ItemAndAmount(stack = it.value[0].itemStack,amount = it.value.size, wasUsed = false) }

        if (shapelessInventory.size != components.size) return false


        return components.all { component ->
            // Gather all stacks in the inventory that match the component
            val matchingStacks = shapelessInventory.sumBy {
                if(!it.wasUsed && component.ingredient.matches(it.stack)) it.amount.apply {
                    // To make sure we don't use the same stack in multiple components
                    it.wasUsed = true
                } else 0
            }

            return@all matchingStacks == component.amount
//            // Ensure it is
//            // Check every component exists in the inventory with the required amount
//            shapelessInventory.any { component.ingredient.matches(it.stack) && component.amount == it.amount }
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