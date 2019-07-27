package spatialcrafting.hologram

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.SidedInventoryFixedWrapper
import net.minecraft.entity.player.PlayerEntity

/**
 * For interfacing with vanilla
 */
class HologramInventoryWrapper(inv : FixedItemInv) : SidedInventoryFixedWrapper(inv) {
    override fun canPlayerUseInv(player: PlayerEntity) = true

}