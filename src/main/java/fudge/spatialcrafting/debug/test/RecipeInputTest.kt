package fudge.spatialcrafting.debug.test

import crafttweaker.api.item.IIngredient
import crafttweaker.mc1120.item.MCItemStack
import fudge.spatialcrafting.SpatialCrafting
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

    init {
        testStack = ItemStack(testItem, 10, OreDictionary.WILDCARD_VALUE)
        testIngredient = MCItemStack(testStack)
    }

    fun test() {

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
        val nbt = RecipeUtil.iIngredientToNbt(testIngredient);
        val ingredientBack = RecipeUtil.ingredientFromNbt(nbt)

        if (testIngredient != ingredientBack) {
            SpatialCrafting.LOGGER.error("testIIngredientConversion test failed! orig was $testIngredient and the one returned was $ingredientBack")
        } else {
            SpatialCrafting.LOGGER.info("testIIngredientConversion test successful!")
        }
    }


}


