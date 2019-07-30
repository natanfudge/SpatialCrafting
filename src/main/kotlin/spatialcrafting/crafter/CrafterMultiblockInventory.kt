package spatialcrafting.crafter

import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import spatialcrafting.recipe.ComponentPosition

data class CrafterMultiblockInventory(private val slots: List<CrafterMultiblockInventorySlot>)
    : List<CrafterMultiblockInventorySlot> by slots{
}

data class CrafterMultiblockInventorySlot(override val position: ComponentPosition, val itemStack: ItemStack)
    : CopyableWithPosition<CrafterMultiblockInventorySlot> {
    override fun copy(newPosition: ComponentPosition) = copy(position = newPosition)
}

interface CopyableWithPosition<out T> where  T : CopyableWithPosition<T> {
    fun copy(newPosition: ComponentPosition): T
    val position: ComponentPosition

}

fun <T: CopyableWithPosition<T>>List<T>.sortedByXYZ()=
    sortedBy { it.position.x }.sortedBy { it.position.y }.sortedBy { it.position.z }

