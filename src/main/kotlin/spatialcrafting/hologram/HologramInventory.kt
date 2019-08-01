package spatialcrafting.hologram

import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.minecraft.item.ItemStack

class HologramInventory : SimpleFixedItemInv(1) {
    override fun getMaxAmount(slot: Int, stack: ItemStack?) = 1

}