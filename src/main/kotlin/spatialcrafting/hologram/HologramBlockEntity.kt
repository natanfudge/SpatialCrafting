package spatialcrafting.hologram

import alexiil.mc.lib.attributes.AttributeList
import net.minecraft.block.entity.BlockEntity
import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.entity.ItemEntity
import spatialcrafting.Packets
import spatialcrafting.sendPacket
import spatialcrafting.util.kotlinwrappers.Builders
import spatialcrafting.util.kotlinwrappers.copy
import spatialcrafting.util.kotlinwrappers.dropItemStack
import spatialcrafting.util.kotlinwrappers.isServer


class HologramBlockEntity : BlockEntity(Type), BlockEntityClientSerializable {

    companion object {
        val Type = Builders.blockEntityType(HologramBlock) { HologramBlockEntity() }

        private object Keys {
            const val Inventory = "inventory"
            const val LastChangeTime = "last_change_time"
        }
    }


    val inventory = HologramInventory().also {
        // Since inventory is only changed at server side we need to send a packet to the client
        // Note: this will be called again in the client after we do that, so we need to ignore it that time.
        it.setOwnerListener { inv, slot, previousStack, currentStack ->
            if (world!!.isClient) return@setOwnerListener

            if (!previousStack.isItemEqual(currentStack)) {
                PlayerStream.watching(this).sendPacket(Packets.UpdateHologramContent(this.pos, currentStack))
            }
        }
    }


    /**
     * This is just used for client sided rendering of the block so the items in the holograms don't move in sync.
     */
    var lastChangeTime: Long = 0

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
        markDirty()
        assert(isEmpty())
        lastChangeTime = world!!.time
        inventory.insert(itemStack.copy(count = 1))
    }

    fun getItem(): ItemStack = inventory.getInvStack(0)

    /**
     * May return an empty stack
     */
    fun extractItem(): ItemStack = inventory.extract(1).also { markDirty() }

    fun isEmpty() = getItem().isEmpty

    fun registerInventory(to: AttributeList<*>) = inventory.offerSelfAsAttribute(to, null, null)


    fun dropInventory() = world!!.dropItemStack(getItem(), pos)

}

