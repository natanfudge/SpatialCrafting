package spatialcrafting.client.keybinding

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import spatialcrafting.modId

val RecipeCreatorKeyBinding: FabricKeyBinding = FabricKeyBinding.Builder.create(modId("open_recipe_creator"),
        InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "Spatial Crafting").build()