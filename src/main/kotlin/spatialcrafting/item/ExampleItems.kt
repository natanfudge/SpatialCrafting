package spatialcrafting.item

import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.SwordItem
import net.minecraft.recipe.Ingredient
import spatialcrafting.SpatialCraftingItemGroup
import spatialcrafting.util.ToolMaterialImpl

val ShapelessSword = SwordItem(ToolMaterialImpl(
        _miningLevel = 3,
        _durability = 3,
        _miningSpeed = 6.5f,
        _attackDamage = 3.0f,
        _enchantability = 0
) { Ingredient.ofItems(Items.MUSHROOM_STEW) }
        , 20,
        -1.0f,
        Item.Settings().group(SpatialCraftingItemGroup)
)

