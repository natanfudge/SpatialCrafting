package spatialcrafting.hologram

import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.minecraft.item.ItemStack

class HologramInventory : SimpleFixedItemInv(1){
    //TODO: restore this once crash is fixed (can update to 0.4.10)
    override fun getMaxAmount(slot: Int, stack: ItemStack?) = 1

}