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


    override fun getInvMaxStackAmount() = 1

    override fun isValidInvStack(slot: Int, stack: ItemStack): Boolean {
        if (slot >= invSize || !getInvStack(slot).isEmpty) return false
        val multiblock = multiblock ?: return false
        // Only accept items if they match when recipe help is active
        return multiblock.helpRecipeComponents(world)?.get(slot)?.ingredient?.matches(stack) ?: true
    }

    /**
     * Returns the inventory size.
     */
    override fun getInvSize(): Int = multiblock?.let {
        it.helpRecipeComponents(world)?.size ?: it.totalHologramAmount
    } ?: 0

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    override fun isInvEmpty() = multiblock?.getHologramEntities(world)?.all { it.isEmpty() } ?: true

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
    override fun getInvStack(slot: Int): ItemStack = getHologramForSlot(slot).getItem().copy()


    /**
     * Takes a stack of the size from the slot.
     *
     * (default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        if (count <= 0) return ItemStack.EMPTY
        else return removeInvStack(slot)
    }

    /**
     * Removes the current stack in the `slot` and returns it.
     */
    override fun removeInvStack(slot: Int): ItemStack = getHologramForSlot(slot).extractItem() ?: ItemStack.EMPTY


    /**
     * Replaces the current stack in the `slot` with the provided stack.
     *
     * If the stack is too big for this inventory ([Inventory.getInvMaxStackAmount]),
     * it gets resized to this inventory's maximum amount.
     */
    override fun setInvStack(slot: Int, stack: ItemStack) {
        val hologram = getHologramForSlotOrNull(slot) ?: return

        val multiblock = multiblock ?: return

        // Remove the previous item so we can put a new one instead
        if (!hologram.isEmpty()) hologram.extractItem()

        val amountTaken = hologram.insertItem(stack.copy(count = 1))
        if (amountTaken == 0) return

        // Try to craft when the last slot is filled
        if (multiblock.filledHologramsCount == invSize) {
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

    override fun canPlayerUseInv(player: PlayerEntity): Boolean = multiblock?.canBeUsedByPlayer(player)
            ?: false

    override fun getInvAvailableSlots(direction: Direction): IntArray = (0 until invSize).toList().toIntArray()

    override fun canInsertInvStack(slot: Int, stack: ItemStack, direction: Direction?): Boolean {
        return isValidInvStack(slot, stack)
    }

    override fun canExtractInvStack(slot: Int, stack: ItemStack, direction: Direction): Boolean {
        return slot < invSize
    }

}