package spatialcrafting.util

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
inline fun initCommon(modId: String, group: ItemGroup? = null, init: CommonModInitializationContext.() -> Unit) {
    CommonModInitializationContext(modId, group).init()
}


inline fun initClientOnly(modId: String, init: ClientModInitializationContext.() -> Unit) {
    ClientModInitializationContext(modId).apply {
        init()
        registerS2C(Packet.InbuiltS2CPackets)
    }

}


class CommonModInitializationContext(val modId: String, val group: ItemGroup?) {
    inline fun <T> registerTo(registry: Registry<T>, init: RegistryContext<T>.() -> Unit) {
        init(RegistryContext(modId, registry))
    }

    inline fun registerBlocksWithItemBlocks(init: BlockWithItemRegistryContext.() -> Unit) {
        init(BlockWithItemRegistryContext(modId, group))
    }

}

class ClientModInitializationContext(val modId: String) {
    inline fun <reified T : BlockEntity> register(renderer: BlockEntityRenderer<T>) {
        BlockEntityRendererRegistry.INSTANCE.register(T::class.java, renderer)
    }

    fun register(keyBinding: FabricKeyBinding) = KeyBindingRegistry.INSTANCE.register(keyBinding)

}


open class RegistryContext<T>(private val namespace: String, private val registry: Registry<T>) {
    open infix fun T.withId(name: String): T = Registry.register(registry, Identifier(namespace, name), this)
    open infix fun T.withId(id: Identifier): T = Registry.register(registry, id, this)
}

class BlockWithItemRegistryContext(private val namespace: String, private val group: ItemGroup?) {
    infix fun Block.withId(name: String) {
        Registry.register(Registry.BLOCK, Identifier(namespace, name), this)
        Registry.register(
                Registry.ITEM,
                Identifier(namespace, name),
                BlockItem(this, Item.Settings().group(group ?: ItemGroup.MISC))
        )
    }
}