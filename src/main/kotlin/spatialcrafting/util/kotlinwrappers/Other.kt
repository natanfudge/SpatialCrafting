package spatialcrafting.util.kotlinwrappers

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

fun PlayerEntity.isHoldingItemIn(hand: Hand): Boolean = !getStackInHand(hand).isEmpty

/**
 * Creates a new [ItemStack] with the specified [count].
 */
fun ItemStack.copy(count: Int) = copy().apply { this.count = count }


/**
 * Converts this into an [ItemStack] that holds exactly one of this.
 */
val ItemConvertible.itemStack get() = ItemStack(this)