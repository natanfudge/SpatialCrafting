package spatialcrafting.crafter

import net.minecraft.inventory.BasicInventory
import net.minecraft.item.ItemStack

class CrafterMultiblockInventoryWrapper(private val inventory: CrafterMultiblockInventory)
    : List<CrafterMultiblockInventorySlot> by inventory,
        // We don't really care above the inventory, we just want to have the inventory interface so we can use it in recipes
        BasicInventory(ItemStack.EMPTY) {
}
