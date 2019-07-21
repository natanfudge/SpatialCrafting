package spatialcrafting

import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem


val FABRIC_ITEM: Item = FabricItem(Item.Settings().group(ItemGroup.MISC))
val EXAMPLE_BLOCK = CrafterPiece(Block.Settings.of(Material.STONE))

@Suppress("unused")
fun init() {
    Registry.register(Registry.ITEM, Identifier("spatialcrafting", "fabric_item"), FABRIC_ITEM)
    Registry.register(Registry.BLOCK, Identifier("spatialcrafting", "example_block"), EXAMPLE_BLOCK)
    Registry.register(Registry.ITEM, Identifier("spatialcrafting", "example_block"),
            BlockItem(EXAMPLE_BLOCK, Item.Settings().group(ItemGroup.MISC)))
    Registry.register(Registry.BLOCK_ENTITY, "spatialcrafting:demo", DEMO_BLOCK_ENTITY_TYPE)
}
