//package spatialcrafting
//
//import net.minecraft.inventory.CraftingInventory
//import net.minecraft.item.ItemStack
//import net.minecraft.recipe.*
//import net.minecraft.util.DefaultedList
//import net.minecraft.util.Identifier
//import net.minecraft.util.math.Vec3i
//import net.minecraft.world.World
//
//data class SpatialRecipe(private val components: List<RecipeComponent>, val output : ItemStack) : Recipe<{
//    override fun craft(var1: CraftingInventory?): ItemStack {
//        TODO("not implemented")
//    }
//
//    override fun getId(): Identifier {
//        TODO("not implemented")
//    }
//
//    override fun fits(var1: Int, var2: Int): Boolean {
//        TODO("not implemented")
//    }
//
//    override fun getSerializer(): RecipeSerializer<*> {
//        TODO("not implemented")
//    }
//
//    override fun getOutput(): ItemStack {
//        TODO("not implemented")
//    }
//
//    override fun matches(var1: CraftingInventory?, var2: World?): Boolean {
//        TODO("not implemented")
//    }
//
//    companion object Serializer {
//
//    }
//}
//
//
//data class RecipeComponent(val position: ComponentPosition, val ingredient : Ingredient)
//data class ComponentPosition(val x: Int, val y: Int, val z: Int)
//
////val SHAPED: RecipeSerializer<ShapedRecipe> = ("spatial_crafting", ShapedRecipe.Serializer())
//
