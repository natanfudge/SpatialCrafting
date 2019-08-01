package spatialcrafting.util.kotlinwrappers

import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry

//package spatialcrafting.util.kotlinwrappers
//
//import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
//import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
//import net.fabricmc.fabric.api.network.PacketContext
//import net.minecraft.block.Block
//import net.minecraft.block.entity.BlockEntity
//import net.minecraft.block.entity.BlockEntityType
//import net.minecraft.client.render.block.entity.BlockEntityRenderer
//import net.minecraft.item.BlockItem
//import net.minecraft.item.Item
//import net.minecraft.item.ItemGroup
//import net.minecraft.util.Identifier
//import net.minecraft.util.PacketByteBuf
//import net.minecraft.util.registry.Registry
//
//object spatialcrafting.util.kotlinwrappers.ModInit {
//    /**
//     * Should be called at the init method of the mod. Do all of your registry here.
//     */
//    inline fun begin(modId: String, init: spatialcrafting.util.kotlinwrappers.ModInitializationContext.() -> Unit) = spatialcrafting.util.kotlinwrappers.ModInitializationContext(modId).init()
//}
//
///**
// * In all functions that accept a string, assume they DON'T need a modId, since you already supplied it.
// */
//class spatialcrafting.util.kotlinwrappers.ModInitializationContext(private val modId: String) {
//    /**
//     * Registers a block, and also creates a [BlockItem] of it and registers that too.
//     * @param id the lower-case name of the block
//     * @param group Where the block is placed in the creative menu
//     */
//    fun <T : Block> T.registerWithBlockItem(id: String, group: ItemGroup = ItemGroup.MISC): T {
//        this.registerWithoutBlockItem(id)
//        BlockItem(this, Item.Settings().group(group)).register(Registry.ITEM, id)
//        return this
//    }
//
//
//    fun <T> T.register(type: Registry<T>, id: String): T = Registry.register(type, Identifier(modId, id), this)
//
//    fun BlockEntityType<*>.register(id: String): BlockEntityType<*> = register(Registry.BLOCK_ENTITY, id)
//    fun Item.register(id: String): Item = register(Registry.ITEM, id)
//    fun Block.registerWithoutBlockItem(id: String): Block = register(Registry.BLOCK, id)
//
//    inline fun <reified T : BlockEntity> BlockEntityRenderer<T>.register() = BlockEntityRendererRegistry.INSTANCE.register(T::class.java, this)
//
//
//    fun registerServerToClientPacket(packetId: Identifier, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
//            ClientSidePacketRegistry.INSTANCE.register(packetId, packetConsumer)
//
//    fun registerServerToClientPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
//            ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
//}
//
//
////inline fun initializeMod(modId: String, init: spatialcrafting.util.kotlinwrappers.ModInitializationContext.() -> Unit) = spatialcrafting.util.kotlinwrappers.ModInitializationContext(modId).init()

//val `asd e` = 2
object ModInit {
    /**
     * Should be called at the init method of the mod. Do all of your registry here.
     */
    inline fun begin(modId: String, group: ItemGroup, init: ModInitializationContext.() -> Unit) =
            ModInitializationContext(modId, group).init()
}

class ModInitializationContext(private val modId: String, private val group: ItemGroup) {
    fun <T> registering(registry: Registry<T>, dsl: NamespacedRegistryDsl<T>.() -> Unit) =
            dsl(NamespacedRegistryDsl(modId, registry))

    fun registeringWithItemBlocks(dsl: BlockWithItemRegistryDsl.() -> Unit) =
            dsl(BlockWithItemRegistryDsl(modId, group))

    inline fun <reified T : BlockEntity> register(renderer: BlockEntityRenderer<T>) = BlockEntityRendererRegistry.INSTANCE.register(T::class.java, renderer)

//        fun registerServerToClientPacket(packetId: Identifier, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
//            ClientSidePacketRegistry.INSTANCE.register(packetId, packetConsumer)

    fun registerServerToClientPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)

//    inline fun <reified T : BlockEntity> register(renderer: BlockEntityRenderer<T>) =
//            BlockEntityRendererRegistry.INSTANCE.register(T::class.java, renderer)

}

//fun id(namespace: String, path: String) = Identifier(namespace, path)
//open class RegistryDsl<T>(protected val registry: Registry<T>) {
////    fun id(namespace: String, path: String) = Identifier(namespace, path)
//    infix fun T.named(id: Identifier): T = Registry.register(registry, id, this)
//}

open class NamespacedRegistryDsl<T>(private val namespace: String, private val registry: Registry<T>) {
    open infix fun T.named(name: String): T = Registry.register(registry, Identifier(namespace, name), this)
}

class BlockWithItemRegistryDsl(private val namespace: String, private val group: ItemGroup) {
    infix fun Block.named(name: String): Block {
        Registry.register(Registry.BLOCK, Identifier(namespace, name), this)
        Registry.register(Registry.ITEM, Identifier(namespace, name), BlockItem(this, Item.Settings().group(group)))
        return this
    }
}