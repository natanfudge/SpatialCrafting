@file:UseSerializers(ForItemStack::class, ForIdentifier::class, ForIngredient::class)
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
import spatialcrafting.crafter.CopyableWithPosition
import spatialcrafting.crafter.CrafterMultiblockInventoryWrapper
import spatialcrafting.crafter.sortedByXYZ
import spatialcrafting.util.Duration
import spatialcrafting.util.matches

@Serializable
class ShapedSpatialRecipe private constructor(val components: List<ShapedRecipeComponent>,
                          override val minimumCrafterSize: Int,
                          override val energyCost: Long,
                          override val _craftTime: Long,
                          override val outputStack: ItemStack,
                          override val identifier: Identifier) : SpatialRecipe() {
    constructor(components: List<ShapedRecipeComponent>,
                minimumCrafterSize: Int,
                energyCost: Long,
                craftTime: Duration,
                outputStack: ItemStack,
                identifier: Identifier,
                workaround: Byte = 0.toByte()
    ) : this(components, minimumCrafterSize, energyCost, craftTime.inTicks, outputStack, identifier)

    override val previewComponents: List<ShapedRecipeComponent>
        get() = components

    override fun matches(inventoryWrapper: CrafterMultiblockInventoryWrapper, world: World): Boolean {
        if (inventoryWrapper.size != this.components.size) return false
        if (inventoryWrapper.crafterSize < minimumCrafterSize) return false
        val inventory = inventoryWrapper.normalizePositions()
        val recipe = components.normalizePositions()

        // Make sure they have been sorted before, as it's a requirement.
        spatialcrafting.util.assert { inventory.sortedByXYZ() == inventory && recipe.sortedByXYZ() == recipe }

        // Note: because currently "minimumCrafterSize" is determined by the recipe side,
        // this comparison already does not accept inventories that their crafterSize are too small.

        return inventory.zip(recipe).all { it.second.ingredient.matches(it.first.itemStack) }

    }

    /**
     * This basically moves the positions to the corner, so no matter how the slots are positioned in space,
     * what matters is the shape. This allows smallers recipes to be crafted on bigger crafters, and generally
     * makes things easier for the player.
     */
    private fun <T : CopyableWithPosition<T>> List<CopyableWithPosition<T>>.normalizePositions(): List<T> {
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

    override fun getSerializer() = Serializer

    companion object Serializer : SpatialRecipe.Serializer<ShapedSpatialRecipe>() {
        override val serializer : KSerializer<ShapedSpatialRecipe>
            get() = serializer()

        override fun build(components: List<ShapedRecipeComponent>, id: Identifier, output: ItemStack,
                           minimumCrafterSize: Int, energyCost: Long, craftTime: Duration): ShapedSpatialRecipe {
            return ShapedSpatialRecipe(
                    components = components,
                    outputStack = output,
                    identifier = id,
                    minimumCrafterSize = minimumCrafterSize,
                    craftTime = craftTime,
                    energyCost = energyCost

            )
        }

    }
}
@Serializable
data class ShapedRecipeComponent(override val position: ComponentPosition, val ingredient: Ingredient)
    : CopyableWithPosition<ShapedRecipeComponent> {
    override fun copy(newPosition: ComponentPosition) = copy(position = newPosition)

    override fun equals(other: Any?) = other is ShapedRecipeComponent &&
            other.position == this.position && other.ingredient.ids == this.ingredient.ids

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + ingredient.hashCode()
        return result
    }

}


// The 'x' 'y' 'z' coordinates of are offset based, meaning they range from 0 to 4, based on how big the multiblock is.
@Serializable
data class ComponentPosition(val x: Int, val y: Int, val z: Int)



