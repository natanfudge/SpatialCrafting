package spatialcrafting.util.kotlinwrappers

import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry

/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
inline fun initCommon(modId: String, group: ItemGroup? = null, init: CommonModInitializationContext.() -> Unit) =
        CommonModInitializationContext(modId, group).init()

inline fun initClientOnly(modId: String, init: ClientModInitializationContext.() -> Unit) = ClientModInitializationContext(modId).init()


class CommonModInitializationContext(val modId: String, val group: ItemGroup?) {
    inline fun <T> registerTo(registry: Registry<T>, dsl: NamespacedRegistryDsl<T>.() -> Unit) =
            dsl(NamespacedRegistryDsl(modId, registry))

    inline fun registerBlocksWithItemBlocks(dsl: BlockWithItemRegistryDsl.() -> Unit) =
            dsl(BlockWithItemRegistryDsl(modId, group))


    fun registerClientToServerPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ServerSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)


}

class ClientModInitializationContext(private val modId: String) {
    inline fun <reified T : BlockEntity> register(renderer: BlockEntityRenderer<T>) = BlockEntityRendererRegistry.INSTANCE.register(T::class.java, renderer)

    fun registerServerToClientPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)

}

//@Serializable
//data class X(val y :Int)
//
//fun X.Companion.shit(){}
//val y = X.shit()

//val x = Json.stringify()


open class NamespacedRegistryDsl<T>(private val namespace: String, private val registry: Registry<T>) {
    open infix fun T.withId(name: String): T = Registry.register(registry, Identifier(namespace, name), this)
    open infix fun T.withId(id : Identifier): T = Registry.register(registry, id, this)
}

class BlockWithItemRegistryDsl(private val namespace: String, private val group: ItemGroup?) {
    infix fun Block.withId(name: String): Block {
        Registry.register(Registry.BLOCK, Identifier(namespace, name), this)
        Registry.register(Registry.ITEM, Identifier(namespace, name), BlockItem(this, Item.Settings().group(group
                ?: ItemGroup.MISC)))
        return this
    }
}