package spatialcrafting.crafter

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.SidedInventoryFixedWrapper
import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.BasicInventory
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class CrafterMultiblockInventoryWrapper(private val inventory: CrafterMultiblockInventory)
    : List<CrafterMultiblockInventorySlot> by inventory,
        // We don't really care above the inventory, we just want to have the inventory interface so we can use it in recipes
        BasicInventory(ItemStack.EMPTY){
}
