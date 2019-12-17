package spatialcrafting.client.gui

import fabricktx.api.getMinecraftClient
import io.github.cottonmc.cotton.gui.GuiDescription
import io.github.cottonmc.cotton.gui.client.CottonClientScreen


class RecipeCreatorScreen(description: GuiDescription) : CottonClientScreen(description){
    override fun keyPressed(ch: Int, keyCode: Int, modifiers: Int): Boolean {
        if(getMinecraftClient().options.keyInventory.matchesKey(ch,keyCode)){
            getMinecraftClient().openScreen(null)
            return true
        }
        return super.keyPressed(ch, keyCode, modifiers)
    }

}
