package fudge.spatialcrafting.debug.test

import fudge.spatialcrafting.SpatialCrafting


object Test {

    fun testInit(): Boolean {
        val test1 = RecipeInputTest()
        try {
            test1.test()
            return true
        } catch (e: Exception) {
            SpatialCrafting.LOGGER.error("Error executing test!", e)
            return false
        }
    }


}

