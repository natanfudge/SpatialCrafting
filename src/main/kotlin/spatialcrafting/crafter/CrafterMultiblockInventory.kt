package spatialcrafting.crafter

import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import spatialcrafting.recipe.ComponentPosition

data class CrafterMultiblockInventory(val slots : List<CrafterMultiblockInventorySlot>)

data class CrafterMultiblockInventorySlot(val position: ComponentPosition, val itemStack : ItemStack)