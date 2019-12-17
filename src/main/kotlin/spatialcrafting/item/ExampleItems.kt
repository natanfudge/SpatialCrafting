package spatialcrafting.item

import fabricktx.api.Builders
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import spatialcrafting.SpatialCraftingItemGroup


val ShapelessSword = Builders.sword(durability = 3, attackSpeed = 3f, damage = 15, enchantability = 0,
        itemGroup = SpatialCraftingItemGroup) {
    Ingredient.ofItems(Items.MUSHROOM_STEW)
}

val DeceptivelySmallSword = Builders.sword(durability = 3000, attackSpeed = 0.3f, damage = 25, enchantability = 10,
        itemGroup = SpatialCraftingItemGroup) {
    Ingredient.ofItems(Items.IRON_BLOCK)
}

val PointyStick = Builders.sword(durability = 100, attackSpeed = 1.8f, damage = 3, enchantability = 3,
        itemGroup = SpatialCraftingItemGroup) {
    Ingredient.ofItems(Items.STICK)
}