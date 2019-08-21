package spatialcrafting.hologram

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.SidedInventoryFixedWrapper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import spatialcrafting.util.d


private const val MAXIMUM_DISTANCE_TO_HOLOGRAM = 100

fun PlayerEntity.isCloseEnoughToHologramPos(pos: BlockPos) = squaredDistanceTo(pos.x.d + 0.5, pos.y.d + 0.5, pos.z.d + 0.5) <= MAXIMUM_DISTANCE_TO_HOLOGRAM


/**
 * For interfacing with vanilla
 */
class HologramInventoryWrapper(inv: FixedItemInv, val pos : BlockPos) : SidedInventoryFixedWrapper(inv) {
    override fun canPlayerUseInv(player: PlayerEntity) = player.isCloseEnoughToHologramPos(pos)
}