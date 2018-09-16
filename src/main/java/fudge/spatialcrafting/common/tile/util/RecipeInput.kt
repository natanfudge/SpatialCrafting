package fudge.spatialcrafting.common.tile.util

import crafttweaker.api.item.IIngredient
import crafttweaker.api.minecraft.CraftTweakerMC
import net.minecraft.item.ItemStack
import java.util.*




class RecipeInput(size: Int, init: (Int, Int, Int) -> IIngredient?) : CubeArr<IIngredient?>(size, init) {

    companion object {
        fun fromArr(arr : Array<Array<Array<IIngredient>>>) : RecipeInput{
            return RecipeInput(arr.size){ i,j,k -> arr[i][j][k] }
        }
    }



    fun toFormattedString(): String {

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
        builder.append("\n]")

        return builder.toString()
    }


    fun itemStacksOfLayer(layer: Int): List<List<ItemStack>> {
        val list = ArrayList<List<ItemStack>>()

        inForEach(layer) { ingredient ->
            if (ingredient != null) {
                list.add(CraftTweakerMC.getItemStacks(ingredient.items).toList())
            } else {
                list.add(listOf(ItemStack.EMPTY))
            }

        }

        return list
    }

    fun get(offset : Offset) : IIngredient? = get(offset.x,offset.y,offset.z)

}