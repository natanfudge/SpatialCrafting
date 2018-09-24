package fudge.spatialcrafting.debug.test

import crafttweaker.api.item.IIngredient
import crafttweaker.mc1120.item.MCItemStack
import fudge.spatialcrafting.SpatialCrafting
import fudge.spatialcrafting.common.crafting.SpatialRecipe
import fudge.spatialcrafting.common.util.RecipeUtil
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.oredict.OreDictionary
import org.junit.Test


class RecipeInputTest {

    private val testItem: Item = Item.getItemFromBlock(Blocks.ACACIA_FENCE)
    private val testStack: ItemStack
    private val testIngredient: IIngredient
    private val testInput = SpatialRecipe.getRecipes()[3].requiredInput

    init {
        testStack = ItemStack(testItem, 10, OreDictionary.WILDCARD_VALUE)
        testIngredient = MCItemStack(testStack)
    }

    fun test() {
        //testItemStackConversion()
        //testIIngredientConversion()
        // testRecipeInputConversion()
        println("No test!")
    }


    @Test
    fun testItemStackConversion() {
        val nbt = testStack.writeToNBT(NBTTagCompound())
        val stackBack = ItemStack(nbt)

        if (!RecipeUtil.itemStackEquals(testStack, stackBack)) {
            SpatialCrafting.LOGGER.error("testItemStackConversion test failed! orig was $testStack and the one returned was $stackBack")
        } else {
            SpatialCrafting.LOGGER.info("testItemStackConversion test successful!")
        }
    }

    @Test
    fun testIIngredientConversion() {
        val nbt = RecipeUtil.IIngredientToNbt(testIngredient);
        val ingredientBack = RecipeUtil.ingredientFromNbt(nbt)

        if (testIngredient != ingredientBack) {
            SpatialCrafting.LOGGER.error("testIIngredientConversion test failed! orig was $testIngredient and the one returned was $ingredientBack")
        } else {
            SpatialCrafting.LOGGER.info("testIIngredientConversion test successful!")
        }
    }

    /*  @Test
      fun testRecipeInputConversion() {
          val nbt = testInput.writeToNBT(NBTTagCompound())
          //val inputBack = IRecipeInput.fromNBT(nbt)

          if (testInput != inputBack) {
              SpatialCrafting.LOGGER.error("testRecipeInputConversion test failed! orig was $testInput and the one returned was $inputBack")
          } else {
              SpatialCrafting.LOGGER.info("testRecipeInputConversion test successful!")
          }
      }*/


}


/*
fun itemStackToOredictIngredient(stack: ItemStack): IIngredient {
    // if (stack.isEmpty()) return null

    val matchingOreDicts = getOres(stack)
    // Too many oredicts, can't process this addition.
    if (matchingOreDicts.size > 1) {
        null!!
    }
    return if (matchingOreDicts.size == 1) {
        matchingOreDicts[0]
    } else CraftTweakerMC.getIItemStack(stack)
}


fun getOres(origStack: ItemStack): List<IOreDictEntry> {
    val result = ArrayList<IOreDictEntry>()

    for (key in OreDictionary.getOreNames()) {
        for (oredictStack in OreDictionary.getOres(key)) {
            if (oredictStack.item === origStack.item) {
                if (oredictStack.itemDamage == OreDictionary.WILDCARD_VALUE || oredictStack.itemDamage == origStack.itemDamage) {
                    val oredict = CraftTweakerAPI.oreDict.get(key)
                    fixOredictCount(oredict, origStack)
                    result.add(oredict)
                    break
                }
            }
        }
    }

    return result
}



*/



