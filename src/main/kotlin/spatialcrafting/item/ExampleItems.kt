package spatialcrafting.item

import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import spatialcrafting.util.kotlinwrappers.Builders


val ShapelessSword = Builders.sword(durability = 3, attackSpeed = 3f, damage = 15, enchantability = 0) {
    Ingredient.ofItems(Items.MUSHROOM_STEW)
}

val DeceptivelySmallSword = Builders.sword(durability = 3000, attackSpeed = 0.3f, damage = 25, enchantability = 10){
    Ingredient.ofItems(Items.IRON_BLOCK)
}

val PointyStick = Builders.sword(durability = 100, attackSpeed = 1.8f, damage = 3,enchantability = 3){
    Ingredient.ofItems(Items.STICK)
}