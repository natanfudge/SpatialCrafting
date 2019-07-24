package spatialcrafting.util

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

/**
 * In all functions that accept a string, assume they DON'T need a modId, since you already supplied it.
 */
class ModInitializationContext(private val modId: String) {
    /**
     * Registers a block, and also creates a [BlockItem] of it and registers that too.
     * @param id the lower-case name of the block
     * @param group Where the block is placed in the creative menu
     */
    fun <T : Block> T.registerWithBlockItem(id: String, group: ItemGroup = ItemGroup.MISC): T {
        this.register(id)
        BlockItem(this, Item.Settings().group(group)).register(Registry.ITEM, id)
        return this
    }


    fun <T> T.register(type: Registry<T>, id: String): T = Registry.register(type, Identifier(modId, id), this)

    fun BlockEntityType<*>.register(id: String): BlockEntityType<*> = register(Registry.BLOCK_ENTITY, id)
    fun Item.register(id: String): Item = register(Registry.ITEM, id)
    fun Block.register(id: String): Block = register(Registry.BLOCK, id)


    fun registerServerToClientPacket(packetId: Identifier, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ClientSidePacketRegistry.INSTANCE.register(packetId, packetConsumer)

    fun registerServerToClientPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
inline fun initializeMod(modId: String, init: ModInitializationContext.() -> Unit) = ModInitializationContext(modId).init()