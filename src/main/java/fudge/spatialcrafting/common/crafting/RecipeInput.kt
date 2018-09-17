package fudge.spatialcrafting.common.crafting

import crafttweaker.api.item.IIngredient
import crafttweaker.api.minecraft.CraftTweakerMC
import fudge.spatialcrafting.common.crafting.RecipeInput.Companion.EMPTY
import fudge.spatialcrafting.common.tile.util.CubeArr
import fudge.spatialcrafting.common.tile.util.Offset
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraftforge.common.util.Constants
import kotlin.collections.ArrayList

fun IIngredient.getTag() : NBTTagList{
    val tags = NBTTagList()

    this.items.forEach{tags.appendTag(CraftTweakerMC.getItemStack(it).toNBTTagList())}
    return tags

}

fun ingredientFromTag(serializedData: NBTTagList) : IIngredient{
    val list = ArrayList<ArrayList<ItemStack>>(serializedData.tagCount())

    for(nbtListElement in serializedData){
        val nbtList = nbtListElement as NBTTagList
        val itemstacks = ArrayList<ItemStack>(nbtList.tagCount())
        list.add(itemstacks)
        for(nbt in nbtList){
            itemstacks.add(itemStackFromTag(nbt as NBTTagList))
        }
    }

    return  CraftTweakerMC.getIIngredient(list)
}

fun itemStackFromTag(serializedData : NBTTagList) : ItemStack{
    var i = 0

    fun next() = i++

    val id = (serializedData.get(next()) as NBTTagInt).int

    if (id == EMPTY.toInt()) {
        return ItemStack.EMPTY
    } else {
        val count = (serializedData.get(next()) as NBTTagInt).int
        val metaData = (serializedData.get(next()) as NBTTagInt).int
        val itemstack = ItemStack(Item.getItemById(id), count, metaData)

        val itemStackTag = serializedData.get(next())
        val tag = if(itemStackTag is NBTTagInt) null else serializedData.get(next()) as NBTTagCompound
        itemstack.item.readNBTShareTag(itemstack, tag)
        return itemstack
    }
}


fun ItemStack.toNBTTagList(): NBTTagList{

    val data  = NBTTagList()

    if (this.isEmpty) {
        data.appendTag(NBTTagShort(EMPTY))
    } else {
        data.appendTag(NBTTagInt(Item.getIdFromItem(this.item)))
        data.appendTag(NBTTagInt(this.count) )
        data.appendTag(NBTTagInt(this.metadata))
        var nbttagcompound: NBTTagCompound? = null

        if (this.item.isDamageable || this.item.shareTag) {
            nbttagcompound = this.item.getNBTShareTag(this)
        }

        data.appendTag(nbttagcompound ?:NBTTagInt(0))
    }

    return data
}






class RecipeInput(size: Int, init: (Int, Int, Int) -> IIngredient?) : CubeArr<IIngredient?>(size, init) {

    companion object {

        const val NBT = "recipeInputNbt"
        const val EMPTY = (-1).toShort()
        const val NULL  = -1

        fun fromNBT(serializedData: NBTTagCompound) : RecipeInput{

            val dataList = serializedData.getTagList(NBT, Constants.NBT.TAG_LIST)
            val size = (Math.cbrt(dataList.tagCount().toDouble())).toInt()
            return RecipeInput(size) init@{i,j,k->
                val pos = i + size * j + size *size *k

                if((((dataList.get(pos) as NBTTagList).get(0)) as NBTTagInt).int == NULL){
                    return@init null
                }else{
                    return@init ingredientFromTag(dataList.get(pos) as NBTTagList)
                }



            }
        }

        fun fromArr(arr : Array<Array<Array<IIngredient>>>) : RecipeInput {
            return RecipeInput(arr.size) { i, j, k -> arr[i][j][k] }
        }
    }

    // Fix for intellij not recognizing the nullability
    @SuppressWarnings("unused")
    override fun get(height :Int, row:Int, col: Int) = super.get(height,row,col)


    //Note: loses some oredict data.
    fun toNBT(existingData : NBTTagCompound) : NBTTagCompound{
        val nbtList = NBTTagList()
        forEach{
            if(it == null){
                val nullList = NBTTagList()
                nullList.appendTag(NBTTagInt(NULL))
                nbtList.appendTag(nullList)
            }
            else nbtList.appendTag(it.getTag())
        }

        existingData.setTag(NBT,nbtList)

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
    fun get(offset : Offset) = get(offset.y,offset.x,offset.z)

}



