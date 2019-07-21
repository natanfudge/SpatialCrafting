package spatialcrafting

import net.fabricmc.api.ModInitializer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

private val FABRIC_ITEM: Item = FabricItem(Item.Settings().group(ItemGroup.MISC))

fun init() {
    Registry.register(Registry.ITEM, Identifier("spatialcrafting", "fabric_item"), FABRIC_ITEM)
}
