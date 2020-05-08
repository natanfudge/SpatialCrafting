package spatialcrafting.crafter

import fabricktx.api.copy
import fabricktx.api.matches
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.getHologramEntity

/**
 * This makes it so machines can input and output into a singular crafterPiece,
 * and it will interface with the multiblock accordingly.
 */
class CrafterPieceInventoryDelegator(private val pos: BlockPos,
                                     private val world: World,
                                     private val crafterPieceBlock: CrafterPieceBlock) : SidedInventory {
    private val multiblock: CrafterMultiblock? get() = world.getCrafterEntity(pos).multiblockIn


    override fun getMaxCountPerStack() = 1

    override fun isValid(slot: Int, stack: ItemStack): Boolean {
        if (slot >= size() || !getStack(slot).isEmpty) return false
        val multiblock = multiblock ?: return false
        // Only accept items if they match when recipe help is active
        return multiblock.helpRecipeComponents(world)?.get(slot)?.ingredient?.matches(stack) ?: true
    }

    /**
     * Returns the inventory size.
     */
    override fun size(): Int = multiblock?.let {
        it.helpRecipeComponents(world)?.size ?: it.totalHologramAmount
    } ?: 0

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    override fun isEmpty() = multiblock?.getHologramEntities(world)?.all { it.isEmpty() } ?: true

    private fun getHologramForSlotOrNull(slot: Int): HologramBlockEntity? {
        val multiblock = multiblock
                ?: return null

        val recipe = multiblock.helpRecipeComponents(world)
        if (recipe != null) {
            return multiblock.getHologramByRelativePosition(world, recipe[slot].position)
        } else {
            return world.getHologramEntity(multiblock.hologramLocations[slot])
        }
    }

    private fun getHologramForSlot(slot: Int): HologramBlockEntity = getHologramForSlotOrNull(slot)
            ?: throw IllegalArgumentException("Can't provide a stack from CrafterPiece when there is no multiblock.")

    /**
     * Gets the item in the slot.
     */
    override fun getStack(slot: Int): ItemStack = getHologramForSlot(slot).getItem().copy()


    /**
     * Takes a stack of the size from the slot.
     *
     * (default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    override fun removeStack(slot: Int, count: Int): ItemStack {
        if (count <= 0) return ItemStack.EMPTY
        else return removeStack(slot)
    }

    /**
     * Removes the current stack in the `slot` and returns it.
     */
    override fun removeStack(slot: Int): ItemStack = getHologramForSlot(slot).extractItem() ?: ItemStack.EMPTY


    /**
     * Replaces the current stack in the `slot` with the provided stack.
     *
     * If the stack is too big for this inventory ([Inventory.getInvMaxStackAmount]),
     * it gets resized to this inventory's maximum amount.
     */
    override fun setStack(slot: Int, stack: ItemStack) {
        val hologram = getHologramForSlotOrNull(slot) ?: return

        val multiblock = multiblock ?: return

        // Remove the previous item so we can put a new one instead
        if (!hologram.isEmpty()) hologram.extractItem()

        val amountTaken = hologram.insertItem(stack.copy(count = 1))
        if (amountTaken == 0) return

        // Try to craft when the last slot is filled
        if (multiblock.filledHologramsCount == size()) {
            val matchingRecipes = multiblock.getMatchingRecipes(world)
            if (matchingRecipes.isNotEmpty()) crafterPieceBlock.craft(matchingRecipes, world, multiblock, pos, automated = true)
        }


    }

    /**
     * Clears [the item list][.getItems]}.
     */
    override fun clear() {
        val multiblock = multiblock ?: return
        for (hologram in multiblock.getHologramEntities(world)) {
            hologram.extractItem()
        }
    }

    override fun markDirty() {
        // Override if you want behavior.
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean = multiblock?.canBeUsedByPlayer(player)
            ?: false

    override fun getAvailableSlots(direction: Direction): IntArray = (0 until size()).toList().toIntArray()

    override fun canInsert(slot: Int, stack: ItemStack, direction: Direction?): Boolean {
        return isValid(slot, stack)
    }

    override fun canExtract(slot: Int, stack: ItemStack, direction: Direction): Boolean {
        return slot < size()
    }

}