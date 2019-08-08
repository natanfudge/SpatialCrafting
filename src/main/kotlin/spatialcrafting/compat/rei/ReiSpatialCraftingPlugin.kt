package spatialcrafting.compat.rei

import me.shedaniel.rei.api.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Identifier
import spatialcrafting.MaxCrafterSize
import spatialcrafting.MinCrafterSize
import spatialcrafting.id
import spatialcrafting.recipe.SpatialRecipe

class ReiSpatialCraftingPlugin : REIPluginEntry {
    companion object {
        val Id = id("rei_plugin")
        private val CrafterSizes = MinCrafterSize..MaxCrafterSize
    }


    override fun getPluginIdentifier(): Identifier = Id

    override fun onFirstLoad(pluginDisabler: PluginDisabler?) {
        super.onFirstLoad(pluginDisabler)
    }

    override fun registerItems(itemRegistry: ItemRegistry?) {
        super.registerItems(itemRegistry)
    }

    override fun registerPluginCategories(recipeHelper: RecipeHelper) {
//        for(i in CrafterSizes){
        recipeHelper.registerCategory(ReiSpatialCraftingCategory(/*i*/))
//        }

    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper) {
//        for (i in CrafterSizes) {
//            recipeHelper.registerRecipes(ReiCategory.id(i), SpatialRecipe::class.java) { ReiDisplay(it) }
        recipeHelper.registerRecipes(ReiSpatialCraftingCategory.Id, SpatialRecipe::class.java) { ReiSpatialCraftingDisplay(it) }

//        }

    }

    override fun registerBounds(displayHelper: DisplayHelper) {
    }

    override fun registerOthers(recipeHelper: RecipeHelper) {
//        for (i in CrafterSizes) {
////            recipeHelper.registerWorkingStations(ReiCategory.id(i), CraftersPieces.getValue(i).itemStack)
//            recipeHelper.registerWorkingStations(ReiCategory.Id, CraftersPieces.getValue(i).itemStack)
//
//        }

//        recipeHelper.registerSpeedCraftButtonArea(DefaultPlugin.CAMPFIRE) { bounds: Rectangle -> Rectangle(bounds.maxX.toInt() - 16, bounds.y + 6, 10, 10) }

    }

    override fun registerSpeedCraft(recipeHelper: RecipeHelper) {
        recipeHelper.registerSpeedCraftFunctional(ReiSpatialCraftingCategory.Id,
                object : SpeedCraftFunctional<ReiSpatialCraftingDisplay> {
                    override fun getFunctioningFor(): Array<Class<Any>> {
                        return arrayOf()
                    }

                    override fun acceptRecipe(screen: Screen?, recipe: ReiSpatialCraftingDisplay?): Boolean {
                        return true
                    }

                    override fun performAutoCraft(screen: Screen?, recipe: ReiSpatialCraftingDisplay?): Boolean {
                        return true
                    }


                })
//        recipeHelper.registerSpeedCraftFunctional(ReiCategory.Id, object : SpeedCraftFunctional<ReiDisplay> {
//            override fun getFunctioningFor(): Array<Class<*>> {
//                return arrayOf()
////                return arrayOf(InventoryScreen::class.java, CraftingTableScreen::class.java)
//            }
//
//            override fun performAutoCraft(screen: Screen, display: ReiDisplay): Boolean {
////                if (display.recipe == null) return false
//
//                println("AUTO CRAFT!")
////                if (screen.javaClass.isAssignableFrom(CraftingTableScreen::class.java)) ((screen as CraftingTableScreen).recipeBookGui as RecipeBookGuiHooks).rei_getGhostSlots().reset() else if (screen.javaClass.isAssignableFrom(InventoryScreen::class.java)) ((screen as InventoryScreen).recipeBookGui as RecipeBookGuiHooks).rei_getGhostSlots().reset() else return false
////                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.recipe.get() as Recipe<*>, Screen.hasShiftDown())
//                return true
//            }
//
//            override fun acceptRecipe(screen: Screen?, p1: ReiDisplay?): Boolean {
//                return true
////                return screen is CraftingTableScreen || screen is InventoryScreen /*&& recipe.height < 3 && recipe.width < 3*/
//            }
//        })
    }


}

