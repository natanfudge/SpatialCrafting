package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import net.minecraft.text.Text
import spatialcrafting.Packets
import spatialcrafting.client.gui.widgets.*
import spatialcrafting.compat.rei.getNearestCrafter
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.RecipeCreatorCurrentLayerInactive
import spatialcrafting.sendPacketToServer
import spatialcrafting.util.getMinecraftClient
import spatialcrafting.util.playButtonClickSoundToClient

class RecipeCreatorGui : LightweightGuiDescription() {
    init {
        val nearestCrafter = getNearestCrafter(getMinecraftClient().world, getMinecraftClient().player.pos)
                ?: error("Crafter GUI opened without a crafter multiblock")

        var doAsdf = true

        drawWidgets(64, 60) {
            if(doAsdf) Text("asdf")
            Text("Toggle asdf").onClick {
                doAsdf = !doAsdf
                recompose()
            }
//            Column(MainAxisAlignment.Center) {
//                DisableableImage(
//                        enabledTexture = "gui/button/up_on.png",
//                        disabledTexture = "gui/button/up_off.png",
//                        width = 13,
//                        height = 11,
//                        enabled = nearestCrafter.recipeCreatorCurrentLayer < nearestCrafter.multiblockSize - 1
//                ).onClick {
//                    if (nearestCrafter.recipeCreatorCurrentLayer < nearestCrafter.multiblockSize - 1) {
//                        playButtonClickSoundToClient()
//                        changeCurrentLayer(nearestCrafter, change = +1)
//                    }
//                }
//
//                VerticalSpace(5)
//                DisableableImage(
//                        enabledTexture = "gui/button/down_on.png",
//                        disabledTexture = "gui/button/down_off.png",
//                        width = 13,
//                        height = 11,
//                        enabled = nearestCrafter.recipeCreatorCurrentLayer > 0
//                ).onClick {
//                    if (nearestCrafter.recipeCreatorCurrentLayer > 0) {
//                        playButtonClickSoundToClient()
//                        changeCurrentLayer(nearestCrafter, change = -1)
//                    }
//                }
//            }

        }
    }
//
//    private fun WidgetContext.HoverableImage(path: String, width: Int, height: Int): DevWidget {
//        return Image(path = path, width = width, height = height).onHover {
//            ScreenDrawing.rect(constraints.x, constraints.y, constraints.width, constraints.height, 0x40_00_00_FF)
//        }
//    }
//
//    private fun WidgetContext.DisableableImage(enabledTexture: String,
//                                               disabledTexture: String,
//                                               width: Int,
//                                               height: Int,
//                                               enabled: Boolean) =
//            //TODO: this doesn't work because we only get one dev widget. need to change something so the actual dev widgets change!
//            if (enabled) HoverableImage(enabledTexture, width, height)
//            else Image(disabledTexture, width, height)


//    private fun RuntimeWidget.changeCurrentLayer(nearestCrafter: CrafterMultiblock, change: Int) {
//        if (nearestCrafter.recipeCreatorCurrentLayer == RecipeCreatorCurrentLayerInactive) {
//            nearestCrafter.recipeCreatorCurrentLayer = 0
//        }
//        else {
//            nearestCrafter.recipeCreatorCurrentLayer += change
//        }
//
//        recompose()
//
//
//        sendPacketToServer(
//                Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
//        )
//    }
}