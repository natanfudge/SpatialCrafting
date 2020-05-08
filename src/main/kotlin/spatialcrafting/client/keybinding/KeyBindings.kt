package spatialcrafting.client.keybinding

import fabricktx.api.KotlinKeyBinding
import fabricktx.api.KotlinKeyBindingBuilder
import fabricktx.api.getMinecraftClient
import fabricktx.api.scheduleRenderUpdate
import org.lwjgl.glfw.GLFW
import spatialcrafting.logDebug
import spatialcrafting.modId

const val SpatialCraftingKeyBindingCategory = "Spatial Crafting"


private fun hotkey(name: String, hotkey: Int, init: KotlinKeyBindingBuilder.() -> Unit = {})
        = KotlinKeyBinding.create(modId(name), category = "Spatial Crafting", key = hotkey, init = init)

val RecipeCreatorKeyBinding = hotkey("open_recipe_creator", GLFW.GLFW_KEY_GRAVE_ACCENT)

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
                getMinecraftClient().scheduleRenderUpdate(posInChunk)
            }
        }
    }
}