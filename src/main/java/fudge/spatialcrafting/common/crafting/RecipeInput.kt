package fudge.spatialcrafting.common.crafting

import crafttweaker.CraftTweakerAPI
import crafttweaker.api.item.IIngredient
import crafttweaker.api.item.IItemStack
import crafttweaker.api.minecraft.CraftTweakerMC
import crafttweaker.api.oredict.IOreDictEntry
import fudge.spatialcrafting.common.tile.util.CubeArr
import fudge.spatialcrafting.common.tile.util.Offset
import fudge.spatialcrafting.debug.test.properlyEquals
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants
import net.minecraftforge.oredict.OreDictionary

fun IIngredient.toNbt(): NBTTagList {
    val tags = NBTTagList()

    this.items.forEach {
        val stack = CraftTweakerMC.getItemStack(it)
        tags.appendTag(stack.writeToNBT(NBTTagCompound()))
    }
    return tags

}

fun List<ItemStack>.properlyEquals(other: List<Any?>): Boolean {
    if (other.size != this.size) return false
    for (stackIndex in this.indices) {
        if (!this[stackIndex].properlyEquals(other[stackIndex])) return false
    }

    return true
}


fun ingredientFromNbt(serializedData: NBTTagList): IIngredient? {
    val list = ArrayList<ItemStack>(serializedData.tagCount())

    for (nbtTagElement in serializedData) {
        val serializedItemStack = nbtTagElement as NBTTagCompound
        val itemStack = ItemStack(serializedItemStack)
        list.add(itemStack)
    }

    val x = 2

    return when {
        list.size == 0 -> null
        list.size == 1 -> {
            /*val ingredient =*/ CraftTweakerMC.getIIngredient(list[0])
            //if(ingredient is IItemStack && ingredient.metadata == OreDictionary.WILDCARD_VALUE)
        }
        else -> getOreDictEntryFromList(list)
    }

}

fun getOreDictEntryFromList(list: List<*>): IOreDictEntry? {
    for (ore in OreDictionary.getOreNames()) {
        if (OreDictionary.getOres(ore).properlyEquals(list)) {
            return CraftTweakerAPI.oreDict.get(ore)
        }
    }
    return null
}


class RecipeInput(size: Int, init: (Int, Int, Int) -> IIngredient?) : CubeArr<IIngredient?>(size, init) {

    companion object {

        const val RECIPE_INPUT_NBT = "recipeInputNbt"
        const val EMPTY = (-1).toShort()
        const val NULL = -1

        fun fromNBT(serializedData: NBTTagCompound): RecipeInput {

            //{{{null ,<minecraft:leaves:*>}, {null ,<minecraft:leaves:*>}}, {{null ,null}, {null ,null}}}

            val recipeInputNbt = serializedData.getTagList(RECIPE_INPUT_NBT, Constants.NBT.TAG_LIST)
            val size = (Math.cbrt(recipeInputNbt.tagCount().toDouble())).toInt()

            val recipe = RecipeInput(size) init@{ i, j, k ->
                val retval: IIngredient?

                val pos = (i * size * size) + (j * size) + (k)
                val ingredientNbt = recipeInputNbt.get(pos) as NBTTagList
                retval = if (ingredientNbt.isEmpty) null else ingredientFromNbt(ingredientNbt)

                return@init retval

            }

            return RecipeInput(size) init@{ i, j, k ->
                val pos = (k) + (j * size) + (i * size * size)
                val ingredientNbt = recipeInputNbt.get(pos) as NBTTagList
                if (ingredientNbt.isEmpty) return@init null
                return@init ingredientFromNbt(ingredientNbt)

            }
        }

        fun fromArr(arr: Array<Array<Array<IIngredient>>>): RecipeInput {
            return RecipeInput(arr.size) { i, j, k -> arr[i][j][k] }
        }
    }

    // Fix for intellij not recognizing the nullability
    @SuppressWarnings("unused")
    override fun get(height: Int, row: Int, col: Int) = super.get(height, row, col)


    //Note: loses some oredict data.
    fun writeToNBT(existingData: NBTTagCompound): NBTTagCompound {
        val nbtList = NBTTagList()
        forEach {
            if (it == null) {
                nbtList.appendTag(NBTTagList())
            } else nbtList.appendTag(it.toNbt())
        }

        existingData.setTag(RECIPE_INPUT_NBT, nbtList)
        val s = this.toString()

        return existingData

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
        // Delete the last ","
        builder.deleteCharAt(builder.length - 1)

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

    // Note that it's [y,x,z] and not [x,y,z]!
    fun get(offset: Offset) = get(offset.y, offset.x, offset.z)

}



