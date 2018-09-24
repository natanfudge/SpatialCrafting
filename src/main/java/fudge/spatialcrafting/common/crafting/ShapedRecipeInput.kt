package fudge.spatialcrafting.common.crafting

import crafttweaker.api.item.IIngredient
import crafttweaker.api.item.IItemStack
import crafttweaker.api.minecraft.CraftTweakerMC
import fudge.spatialcrafting.common.crafting.RecipeInputSerialization.RECIPE_INPUT_NBT
import fudge.spatialcrafting.common.tile.util.CraftingInventory
import fudge.spatialcrafting.common.tile.util.CubeArr
import fudge.spatialcrafting.common.util.RecipeUtil
import fudge.spatialcrafting.common.util.RecipeUtil.ingredientFromNbt
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants


class ShapedRecipeInput(size: Int, init: (Int, Int, Int) -> IIngredient?) : CubeArr<IIngredient?>(size, init), IRecipeInput {
    override fun getCorrespondingStack(inventory: CraftingInventory, iitemstacks: CubeArr<IItemStack>, i: Int, j: Int, k: Int): ItemStack {
        return inventory[i, j, k]
    }


    override fun matchesLayer(inventory: CraftingInventory, layer: Int, hologramsActive: Array<BooleanArray>): Boolean {
        val size = cubeSize
        for (i in 0 until size) {
            for (j in 0 until size) {
                val stack = CraftTweakerMC.getIItemStack(inventory[layer, i, j])
                val requiredStack = this[layer, i, j]

                // If the hologram is not active it counts as complete
                if (hologramsActive[i][j] && !RecipeUtil.nullSafeMatch(requiredStack, stack)) {
                    return false
                }

            }
        }
        return true
    }

    override fun matches(inventory: CraftingInventory): Boolean {
        return equalsDifSize(inventory.toIItemStackArr()) { ingredient, itemStack ->
            ingredient?.matches(itemStack) ?: (itemStack == null)

        }
    }

    override fun isEquivalentTo(other: IRecipeInput): Boolean {
        return other is ShapedRecipeInput && equalsDifSize(other)
    }

    private var ingredientAmount: Int = 0


    init {
        forEach {
            if (it != null) ingredientAmount++
        }
    }


    override fun ingredientAmount() = ingredientAmount


    override fun layerSize() = cubeSize


    override fun intersectsWith(other: IRecipeInput): Boolean {
        if (other is ShapedRecipeInput) return this.equalsDifSize(other) lambda@{ newIng, oldIng ->
            if (newIng == null && oldIng == null) return@lambda true
            if (newIng == null || oldIng == null) return@lambda false

            return@lambda newIng.contains(oldIng) || oldIng.contains(newIng)
        } else if (other is ShapelessRecipeInput) {
            return other.intersectsWith(this)
        }

        throw UnsupportedOperationException("Unexpected IRecipeInput subclass!")
    }


    companion object {


        fun fromArr(arr: Array<Array<Array<IIngredient>>>): IRecipeInput {
            return ShapedRecipeInput(arr.size) { i, j, k -> arr[i][j][k] }
        }

        fun fromNBT(serializedData: NBTTagCompound): IRecipeInput {

            val recipeInputNbt = serializedData.getTagList(RECIPE_INPUT_NBT, Constants.NBT.TAG_LIST)
            val size = (Math.cbrt(recipeInputNbt.tagCount().toDouble())).toInt()

            return ShapedRecipeInput(size) init@{ i, j, k ->
                val pos = (k) + (j * size) + (i * size * size)
                val ingredientNbt = recipeInputNbt.get(pos) as NBTTagList
                if (ingredientNbt.isEmpty) return@init null
                return@init ingredientFromNbt(ingredientNbt)

            }
        }

    }


    // Fix for intellij not recognizing the nullability
    @SuppressWarnings("unused")
    override fun get(height: Int, row: Int, col: Int) = super<CubeArr>.get(height, row, col)


    override fun writeToNBT(existingData: NBTTagCompound): NBTTagCompound {
        val nbtList = NBTTagList()
        forEach {
            if (it == null) {
                nbtList.appendTag(NBTTagList())
            } else nbtList.appendTag(RecipeUtil.IIngredientToNbt(it))
        }

        existingData.setTag(RECIPE_INPUT_NBT, nbtList)

        return existingData

    }


    override fun toFormattedString(): String {

        val builder = StringBuilder("[")

        for (arr2D in wrappedArray) {
            builder.append("\n\t[")

            for (arr1D in arr2D) {
                builder.append("\n\t\t[")
                for (ingredient in arr1D) {

                    if (ingredient != null) {
                        builder.append(ingredient.toCommandString())
                    } else {
                        builder.append("      null      ")
                    }

                    builder.append(", ")

                }

                // Delete the last ", "
                builder.delete(builder.length - 2, builder.length)

                builder.append("],")

            }
            // Delete the last ","
            builder.deleteCharAt(builder.length - 1)

            builder.append("\n\t],")
        }
        // Delete the last ","
        builder.deleteCharAt(builder.length - 1)

        builder.append("\n]")

        return builder.toString()
    }

    override fun itemStackOfLayer(layer: Int): MutableList<MutableList<ItemStack>> {
        val list = ArrayList<MutableList<ItemStack>>()

        inForEach(layer) { ingredient ->
            if (ingredient != null) {
                list.add(CraftTweakerMC.getItemStacks(ingredient.items).toMutableList())
            } else {
                list.add(mutableListOf(ItemStack.EMPTY))
            }

        }

        return list
    }


}


