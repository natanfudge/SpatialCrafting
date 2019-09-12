package spatialcrafting.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

object EmptyInventory : SidedInventory {
    override fun getInvAvailableSlots(var1: Direction?): IntArray = IntArray(0)
    override fun canExtractInvStack(var1: Int, var2: ItemStack?, var3: Direction?): Boolean = false
    override fun canInsertInvStack(var1: Int, var2: ItemStack?, var3: Direction?): Boolean = false
    override fun clear() {}
    override fun getInvSize(): Int = 0
    override fun isInvEmpty(): Boolean = true
    override fun getInvStack(var1: Int): ItemStack = ItemStack.EMPTY
    override fun takeInvStack(var1: Int, var2: Int): ItemStack = ItemStack.EMPTY
    override fun removeInvStack(var1: Int): ItemStack = ItemStack.EMPTY
    override fun setInvStack(var1: Int, var2: ItemStack) {}
    override fun markDirty() {}
    override fun canPlayerUseInv(var1: PlayerEntity): Boolean = false
}