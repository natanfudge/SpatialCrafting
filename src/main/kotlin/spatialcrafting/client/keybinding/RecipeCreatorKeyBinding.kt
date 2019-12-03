package spatialcrafting.client.keybinding

import org.lwjgl.glfw.GLFW
import spatialcrafting.modId
import spatialcrafting.util.*

const val SpatialCraftingKeyBindingCategory = "Spatial Crafting"


private fun hotkey(name: String, hotkey: Int, init: KotlinKeyBindingBuilder.() -> Unit = {}) = KotlinKeyBinding.create(modId(name), code = hotkey, init = init)

val RecipeCreatorKeyBinding = hotkey("open_recipe_creator", GLFW.GLFW_KEY_GRAVE_ACCENT)

//TODO: make the floating items 70% smaller while this is active
//TODO: on activation, update in a radius of 1 chunk around, and on release, update in a radius of 2 chunks around.
val MinimizeHologramsKeyBinding = hotkey("minimize_holograms", GLFW.GLFW_KEY_Y) {
    onPressStart {
        logDebug { "Minimize holograms keyBinding press started" }
        scheduleRenderUpdateInChunkRadius(1)
    }
    onReleased {
        logDebug { "Minimize holograms keyBinding press ended" }
        // Here we do a bigger range in case the user moved out
        scheduleRenderUpdateInChunkRadius(2)
    }
}

private const val ChunkSize = 16
// Optimally this would search for nearby multiblocks and only rerender where they are,
// but this doesn't cause that much lag so it's fine right now.
private fun scheduleRenderUpdateInChunkRadius(radius: Int) {
    val pos = getMinecraftClient().player?.blockPos ?: return

    val range = -radius..radius

    for (x in range) {
        for (y in range) {
            for (z in range) {
                val posInChunk = pos.add(x * ChunkSize, y * ChunkSize, z * ChunkSize)
                Client.scheduleRenderUpdate(posInChunk)
            }
        }
    }
}