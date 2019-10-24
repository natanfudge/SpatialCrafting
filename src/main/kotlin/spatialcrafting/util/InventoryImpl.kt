package spatialcrafting.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.DefaultedList
import net.minecraft.util.math.Direction


interface ImplementedInventory : SidedInventory {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    val items: DefaultedList<ItemStack>
    // Inventory
    /**
     * Returns the inventory size.
     */
    override fun getInvSize(): Int {
        return items.size
    }

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    override fun isInvEmpty(): Boolean {
        for (i in 0 until invSize) {
            val stack = getInvStack(i)
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    /**
     * Gets the item in the slot.
     */
    override fun getInvStack(slot: Int): ItemStack {
        return items[slot]
    }

    /**
     * Takes a stack of the size from the slot.
     *
     * (default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        val result = Inventories.splitStack(items, slot, count)
        if (!result.isEmpty) {
            markDirty()
        }
        return result
    }

    /**
     * Removes the current stack in the `slot` and returns it.
     */
    override fun removeInvStack(slot: Int): ItemStack {
        return Inventories.removeStack(items, slot)
    }

    /**
     * Replaces the current stack in the `slot` with the provided stack.
     *
     * If the stack is too big for this inventory ([Inventory.getInvMaxStackAmount]),
     * it gets resized to this inventory's maximum amount.
     */
    override fun setInvStack(slot: Int, stack: ItemStack) {
        items[slot] = stack
        if (stack.count > invMaxStackAmount) {
            stack.count = invMaxStackAmount
        }
    }

    /**
     * Clears [the item list][.getItems]}.
     */
    override fun clear() {
        items.clear()
    }

    override fun markDirty() {
        // Override if you want behavior.
    }

    override fun canPlayerUseInv(player: PlayerEntity): Boolean {
        return true
    }

    override fun getInvAvailableSlots(direction: Direction): IntArray {
        // Just return an array of all slots
        val result = IntArray(items.size)
        for (i in result.indices) {
            result[i] = i
        }

        return result
    }

    override fun canInsertInvStack(slot: Int, stack: ItemStack, direction: Direction?): Boolean {
        return direction !== Direction.UP
    }

   override  fun canExtractInvStack(slot: Int, stack: ItemStack, direction: Direction): Boolean {
        return true
    }

    companion object {
        // Creation
        /**
         * Creates an inventory from the item list.
         */
        fun of(items: DefaultedList<ItemStack>): ImplementedInventory = object : ImplementedInventory {
            override val items = items
        }

        /**
         * Creates a new inventory with the size.
         */
        fun ofSize(size: Int): ImplementedInventory {
            return of(DefaultedList.ofSize(size, ItemStack.EMPTY))
        }
    }
}