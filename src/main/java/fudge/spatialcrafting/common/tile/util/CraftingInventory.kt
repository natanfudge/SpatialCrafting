package fudge.spatialcrafting.common.tile.util

import crafttweaker.api.item.IItemStack
import crafttweaker.api.minecraft.CraftTweakerMC
import net.minecraft.item.ItemStack

class CraftingInventory(size: Int, init: (Int, Int, Int) -> ItemStack) : CubeArr<ItemStack>(size, init) {

    var stackAmount = 0

    init {
        forEach{
            if(!it.isEmpty){
                stackAmount++
            }
        }
    }

    fun toIItemStackArr(): CubeArr<IItemStack?> =
            CubeArr(cubeSize) { i, j, k -> CraftTweakerMC.getIItemStack(get(i, j, k)) }

    /*  fun itemStacksOfLayer(layer: Int): Array<Array<ItemStack>> {

          val arr = Array(cubeSize) { ItemStack.EMPTY }
          return Array(cubeSize) lambda@{
              return@lambda wrappedArray[layer][it].toArray(arr)
          }

      }*/


    override fun isEmpty(): Boolean {
        forEach { if (it != ItemStack.EMPTY) return false }

        return true
    }


}

