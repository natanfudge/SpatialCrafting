package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.GuiDescription
import io.github.cottonmc.cotton.gui.client.ClientCottonScreen
import spatialcrafting.util.getMinecraftClient


class RecipeCreatorScreen(description: GuiDescription) : ClientCottonScreen(description){
    override fun keyPressed(ch: Int, keyCode: Int, modifiers: Int): Boolean {
        if(getMinecraftClient().options.keyInventory.matchesKey(ch,keyCode)){
            getMinecraftClient().openScreen(null)
            return true
        }
        return super.keyPressed(ch, keyCode, modifiers)
    }

}
