@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.LongTag
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IWorld
import net.minecraft.world.World
import kotlin.math.min
import kotlin.math.sqrt


val BlockPos.xz get() = "($x,$z)"

fun BlockPos.distanceFrom(otherPos: Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())

operator fun BlockPos.plus(other: BlockPos): BlockPos = this.add(other)
operator fun BlockPos.plus(vec3d: Vec3d): Vec3d = this.toVec3d() + vec3d
operator fun BlockPos.minus(other: BlockPos): BlockPos = this.subtract(other)
operator fun BlockPos.minus(other: Vec3d): Vec3d = this.toVec3d().subtract(other)

fun BlockPos.toVec3d() = Vec3d(this)


fun CompoundTag.putBlockPos(key: String, pos: BlockPos?) = if (pos != null) putLong(key, pos.asLong()) else Unit

fun CompoundTag.getBlockPos(key: String): BlockPos? {
    val tag = getTag(key) ?: return null
    if (tag !is LongTag) return null
    return BlockPos.fromLong(tag.long)
}

fun vec3d(x: Double, y: Double, z: Double) = Vec3d(x, y, z)
operator fun Vec3d.plus(other: Vec3d) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
operator fun Vec3d.minus(other: Vec3d): Vec3d = this.subtract(other)

fun IWorld.play(soundEvent: SoundEvent, at: BlockPos,
                ofCategory: SoundCategory, toPlayer: PlayerEntity? = null, volumeMultiplier: Float = 1.0f, pitchMultiplier: Float = 1.0f) {
    playSound(toPlayer, at, soundEvent, ofCategory, volumeMultiplier, pitchMultiplier)
}

const val TicksPerSecond = 20


fun IWorld.getBlock(location: BlockPos): Block = getBlockState(location).block

fun IWorld.setBlock(block: Block, pos: BlockPos, blockState: BlockState = block.defaultState): Boolean = world.setBlockState(pos, blockState)

val World.isServer get() = !isClient

val IWorld.name get() = if (isClient) "Client" else "Server"

fun IWorld.dropItemStack(stack: ItemStack, pos: BlockPos): ItemEntity = dropItemStack(stack, pos.toVec3d())


fun IWorld.dropItemStack(stack: ItemStack, pos: Vec3d): ItemEntity =
        ItemEntity(world, pos.x, pos.y, pos.z, stack).also {
            world.spawnEntity(it)
        }


fun ItemStack.copy(count: Int): ItemStack = copy().apply { this.count = count }

val ItemConvertible.itemStack get() = ItemStack(this)

inline fun Ingredient.matches(itemStack: ItemStack) = method_8093(itemStack)

/**
 * Note that what is held in the main hand still exists in the inventory, so it includes that.
 */
val PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand


fun PlayerEntity.isHoldingItemIn(hand: Hand): Boolean = !getStackInHand(hand).isEmpty

fun PlayerEntity.offerOrDrop(itemStack: ItemStack) = inventory.offerOrDrop(world, itemStack)

private fun Inventory.stackIsNotEmptyAndCanAddMore(toStack: ItemStack, stackToAdd: ItemStack): Boolean {
    return !toStack.isEmpty &&
            areItemsEqual(toStack, stackToAdd)
            && toStack.isStackable
            && toStack.count < toStack.maxCount
            && toStack.count < this.invMaxStackAmount
}


/**
 * Returns the remaining stack
 */
fun Inventory.insert(stack: ItemStack, direction: Direction = Direction.UP): ItemStack {
    val remainingAfterNonEmptySlots = distributeToAvailableSlots(stack, acceptEmptySlots = false, direction = direction)
    return distributeToAvailableSlots(remainingAfterNonEmptySlots, acceptEmptySlots = true, direction = direction)
}

fun World.inventoryExistsIn(pos: BlockPos): Boolean = world.getBlock(pos) is InventoryProvider
        || world.getBlockEntity(pos) is Inventory


fun World.getInventoryIn(pos: BlockPos): Inventory? {
    val blockEntityInventory = world.getBlockEntity(pos)

    // Fuck you notch
    if (blockEntityInventory is ChestBlockEntity) {
        val blockState = world.getBlockState(pos)
        if (blockState.block is ChestBlock) {
            return ChestBlock.getInventory(blockState, this, pos, true)
        }
    }

    if (blockEntityInventory is Inventory) return blockEntityInventory
    val blockState = world.getBlockState(pos)
    return (blockState.block as? InventoryProvider)?.getInventory(blockState, this, pos)
}


private fun areItemsEqual(stack1: ItemStack, stack2: ItemStack): Boolean {
    return stack1.item === stack2.item && ItemStack.areTagsEqual(stack1, stack2)
}

private fun Inventory.availableSlots(direction: Direction): Iterable<Int> {
    return if (this is SidedInventory) getInvAvailableSlots(direction).toList() else (0 until invSize)
}

private fun Inventory.canInsert(slot: Int, stack: ItemStack, direction: Direction): Boolean {
    return if (this is SidedInventory) canInsertInvStack(slot, stack, direction) else isValidInvStack(slot, stack)
}

private fun Inventory.distributeToAvailableSlots(stack: ItemStack, acceptEmptySlots: Boolean, direction: Direction): ItemStack {
    val maxStackSize = invMaxStackAmount
    var stackCountLeftToDistribute = stack.count
    for (slot in availableSlots(direction)) {
        if (!canInsert(slot, stack, direction)) continue

        val stackInSlot = getInvStack(slot)
        if ((acceptEmptySlots && stackInSlot.isEmpty) || stackIsNotEmptyAndCanAddMore(stackInSlot, stack)) {
            val amountThatCanFitInSlot = maxStackSize - stackInSlot.count
            if (amountThatCanFitInSlot >= 0) {
                setInvStack(slot, ItemStack(stack.item,
                        min(maxStackSize, stackInSlot.count + stackCountLeftToDistribute)
                ))
                stackCountLeftToDistribute -= amountThatCanFitInSlot
            }
        }

        if (stackCountLeftToDistribute <= 0) return ItemStack.EMPTY

    }

    return stack.copy(count = stackCountLeftToDistribute)
}