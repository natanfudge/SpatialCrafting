package spatialcrafting

import alexiil.mc.lib.attributes.AttributeList
import net.minecraft.block.entity.BlockEntity
import spatialcrafting.util.Builders
import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import spatialcrafting.util.copy
import net.minecraft.command.arguments.ItemStackArgumentType.itemStack
import net.minecraft.entity.ItemEntity
import net.minecraft.predicate.entity.DistancePredicate.y
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World




val HologramBlockEntityType = Builders.blockEntityType(HologramBlock) { HologramBlockEntity() }

class HologramBlockEntity : BlockEntity(HologramBlockEntityType), BlockEntityClientSerializable {
    override fun toClientTag(p0: CompoundTag): CompoundTag = toTag(p0)

    override fun fromClientTag(p0: CompoundTag) = fromTag(p0)

    private val inventory = SimpleFixedItemInv(1)
    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        tag.put("inventory", inventory.toTag());
        return tag
    }

    /**
     * Inserts only one of the itemStack
     */
    fun insertItem(itemStack: ItemStack) {
        assert(isEmpty())
        inventory.insert(itemStack.copy(count = 1))
    }

    fun getItem() = inventory.getInvStack(0)

    /**
     * May return an empty stack
     */
    fun extractItem(): ItemStack = inventory.extract(1)

    fun isEmpty() = getItem().isEmpty

    fun registerInventory(to : AttributeList<*>) = inventory.offerSelfAsAttribute(to, null, null)

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        inventory.fromTag(tag.getCompound("inventory"));
    }

//    fun dropItemStack(world: World, pos: BlockPos, itemStack: ItemStack, randomMotion: Boolean = true) {
//        val itemEntity = ItemEntity(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), getItem())
////        if (!randomMotion) {
////            itemEntity.motionZ = 0
////            itemEntity.motionY = itemEntity.motionZ
////            itemEntity.motionX = itemEntity.motionY
////        }
//        world.spawnEntity(itemEntity)
//    }

    fun dropInventory() {
        val itemEntity = ItemEntity(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), getItem())
        world!!.spawnEntity(itemEntity)
    }

    //TODO: store 1 inventory slot and save/load to disk
}

