package spatialcrafting.crafter

import net.minecraft.inventory.BasicInventory
import net.minecraft.item.ItemStack

//DO NOT make this a list by delegating to inventory (by inventory) because it clashes with the inventory size()
// and crashes at runtime
class CrafterMultiblockInventoryWrapper(val inventory: CrafterMultiblockInventory, val crafterSize: Int) :
        // We don't really care above the inventory, we just want to have the inventory interface so we can use it in recipes
        BasicInventory(ItemStack.EMPTY) {
    override fun isEmpty(): Boolean {
        return inventory.isEmpty()
    }
}

