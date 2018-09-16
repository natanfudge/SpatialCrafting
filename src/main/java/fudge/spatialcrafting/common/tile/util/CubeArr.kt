package fudge.spatialcrafting.common.tile.util

open class CubeArr<T>(val cubeSize: Int, init: (Int, Int, Int) -> T) : Arr3D<T>(cubeSize, cubeSize, cubeSize, init)

