package fudge.spatialcrafting.common.tile.util

import net.minecraft.util.math.BlockPos

class CrafterPoses(val size: Int, init: (i: Int, j: Int) -> BlockPos) {

    private val wrappedArray: Array<Array<BlockPos>>

    init {
        wrappedArray = Array(size) { i -> Array(size) { j -> init(i, j) } }
    }

    fun forEach(action: (blockPos: BlockPos) -> Unit) {
        wrappedArray.forEach { array -> array.forEach(action) }
    }

    fun get(i: Int, j: Int): BlockPos = wrappedArray[i][j]


    fun firstCrafter() = get(0, 0)
    fun lastCrafter() = get(size - 1, size - 1)


}