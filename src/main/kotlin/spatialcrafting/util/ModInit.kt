package spatialcrafting.util

import com.mojang.datafixers.util.Pair
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.class_4730
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.container.PlayerContainer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Function

/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
inline fun initCommon(modId: String, group: ItemGroup? = null, init: CommonModInitializationContext.() -> Unit) {
    CommonModInitializationContext(modId, group).init()
}


/**
 * Should be called at the client init method
 */
inline fun initClientOnly(modId: String, init: ClientModInitializationContext.() -> Unit) {
    ClientModInitializationContext(modId).apply {
        init()
        registerS2C(Packet.InbuiltS2CPackets)
    }

}


class CommonModInitializationContext(@PublishedApi internal val modId: String,
                                     @PublishedApi internal val group: ItemGroup?) {

    inline fun <T> registerTo(registry: Registry<T>, init: RegistryContext<T>.() -> Unit) {
        init(RegistryContext(modId, registry))
    }

    inline fun registerBlocksWithItemBlocks(init: BlockWithItemRegistryContext.() -> Unit) {
        init(BlockWithItemRegistryContext(modId, group))
    }

}

class ClientModInitializationContext(@PublishedApi internal val modId: String) {
    fun Block.setRenderLayer(renderLayer: RenderLayer) = BlockRenderLayerMap.INSTANCE.putBlock(this, renderLayer)

    inline fun <reified T : BlockEntity> registerBlockEntityRenderer(be: BlockEntityType<T>, renderer: BlockEntityRenderer<T>) {
        BlockEntityRendererRegistry.INSTANCE.register(be, renderer)
    }

    fun registerKeyBinding(keyBinding: FabricKeyBinding) = KeyBindingRegistry.INSTANCE.register(keyBinding)
    fun registerKeyBindingCategory(name: String) = KeyBindingRegistry.INSTANCE.addCategory(name)

    fun registerBlockModel(blockPath: String, vararg textures: Identifier, bakery: () -> BakedModel) {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelId, _ ->
                if (modelId.namespace == modId && modelId.path == blockPath) {
                    object : UnbakedModel {
                        override fun getModelDependencies(): List<Identifier> = listOf()

                        override fun bake(loader: ModelLoader?, textureGetter: Function<class_4730, Sprite>?,
                                          rotationContainer: ModelBakeSettings?, modelId: Identifier?): BakedModel? =
                                bakery()

                        override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>?,
                                                            unresolvedTextureReferences: MutableSet<Pair<String, String>>?):
                                List<class_4730> = textures.map { class_4730(PlayerContainer.field_21668, it) }

                    }
                } else null
            }
        }
    }
}


class RegistryContext<T>(private val namespace: String, private val registry: Registry<T>) {
    infix fun T.withId(name: String): T = Registry.register(registry, Identifier(namespace, name), this)
    infix fun T.withId(id: Identifier): T = Registry.register(registry, id, this)
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