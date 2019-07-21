package spatialcrafting

import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.block.Block
import net.minecraft.item.BlockItem

const val ModId = "spatialcrafting"


@Suppress("unused")
fun init() {
    FabricItem.register(Registry.ITEM, id = "fabric_item")
    CrafterPieceX2.registerWithBlockItem(id = "example_block")
    DemoBlockEntityType.register(Registry.BLOCK_ENTITY, id = "demo")
}

private fun <T : Block> T.registerWithBlockItem(id: String, group: ItemGroup = ItemGroup.MISC) {
    this.register(Registry.BLOCK, id)
    BlockItem(this,Item.Settings().group(group)).register(Registry.ITEM,id)
}


private fun <T> T.register(type: Registry<T>, id: String) = Registry.register(type, Identifier(ModId, id), this)