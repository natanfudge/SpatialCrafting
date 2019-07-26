package spatialcrafting.hologram

import alexiil.mc.lib.attributes.AttributeList
import net.minecraft.block.entity.BlockEntity
import spatialcrafting.util.Builders
import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import spatialcrafting.util.copy
import net.minecraft.entity.ItemEntity
import kotlin.properties.Delegates


val HologramBlockEntityType = Builders.blockEntityType(HologramBlock) { HologramBlockEntity() }

class HologramBlockEntity : BlockEntity(HologramBlockEntityType), BlockEntityClientSerializable {
    companion object {
        private object Keys {
            const val Inventory = "inventory"
            const val LastChangeTime = "last_change_time"
        }

    }


    //TODO: document BLOCK ENTITY RENDERER
    private val inventory = SimpleFixedItemInv(1)

    /**
     * This is just used for client sided rendering of the block so the items in the holograms don't move in sync.
     */
    var lastChangeTime: Long by Delegates.notNull()

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        tag.put(Keys.Inventory, inventory.toTag());
        tag.putLong(Keys.LastChangeTime, lastChangeTime)
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        inventory.fromTag(tag.getCompound(Keys.Inventory));
        lastChangeTime = tag.getLong(Keys.LastChangeTime)
    }

    override fun toClientTag(p0: CompoundTag): CompoundTag = toTag(p0)

    override fun fromClientTag(p0: CompoundTag) = fromTag(p0)

    /**
     * Inserts only one of the itemStack
     */
    fun insertItem(itemStack: ItemStack) {
        assert(isEmpty())
        lastChangeTime = world!!.time
        inventory.insert(itemStack.copy(count = 1))
    }

    fun getItem(): ItemStack = inventory.getInvStack(0)

    /**
     * May return an empty stack
     */
    fun extractItem(): ItemStack = inventory.extract(1)

    fun isEmpty() = getItem().isEmpty

    fun registerInventory(to: AttributeList<*>) = inventory.offerSelfAsAttribute(to, null, null)


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

