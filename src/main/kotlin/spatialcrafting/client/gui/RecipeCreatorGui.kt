package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
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

        drawWidgets(64, 60) {

            Row {
                UpDownButtons(nearestCrafter)
                Row(MainAxisAlignment.End) {
                    Column(MainAxisAlignment.Center) {
                        Switch(true)
                        VerticalSpace(5)
                        Switch(false)
                    }
                }
            }


        }
    }

    private fun DevWidget.UpDownButtons(nearestCrafter: CrafterMultiblock) {
        Column(MainAxisAlignment.Center, CrossAxisAlignment.Baseline) {
            if (nearestCrafter.recipeCreatorCurrentLayer != RecipeCreatorCurrentLayerInactive) {
                Text("${nearestCrafter.recipeCreatorCurrentLayer}", color = 0x55_FF_FF)
            }

            DisableableImage(
                    enabledTexture = "gui/button/up_on.png",
                    disabledTexture = "gui/button/up_off.png",
                    width = 13,
                    height = 11,
                    enabled = nearestCrafter.recipeCreatorCurrentLayer < nearestCrafter.multiblockSize - 1
            ) {
                changeCurrentLayer(nearestCrafter, change = +1, recompositionTarget = this@Column)
            }

            VerticalSpace(5)
            DisableableImage(
                    enabledTexture = "gui/button/down_on.png",
                    disabledTexture = "gui/button/down_off.png",
                    width = 13,
                    height = 11,
                    enabled = nearestCrafter.recipeCreatorCurrentLayer > 0
            ) {
                changeCurrentLayer(nearestCrafter, change = -1, recompositionTarget = this@Column)
            }

        }
    }

    private fun DevWidget.HoverableImage(path: String, width: Int, height: Int): DevWidget {
        return Image(path = path, width = width, height = height).onHover {
            ScreenDrawing.rect(constraints.x, constraints.y, constraints.width, constraints.height, 0x40_00_00_FF)
        }
    }

    private fun DevWidget.DisableableImage(enabledTexture: String,
                                           disabledTexture: String,
                                           width: Int,
                                           height: Int,
                                           enabled: Boolean, onClick: RuntimeWidget. () -> Unit) =
            //TODO: this doesn't work because we only get one dev widget. need to change something so the actual dev widgets change!
            if (enabled) HoverableImage(enabledTexture, width, height).onClick {
                playButtonClickSoundToClient()
                onClick()
            }
            else Image(disabledTexture, width, height)


    private fun DevWidget.changeCurrentLayer(nearestCrafter: CrafterMultiblock, change: Int, recompositionTarget: DevWidget) {
        if (nearestCrafter.recipeCreatorCurrentLayer == RecipeCreatorCurrentLayerInactive) {
            nearestCrafter.recipeCreatorCurrentLayer = 0
        }
        else {
            nearestCrafter.recipeCreatorCurrentLayer += change
        }

        recompose(recompositionTarget)


        sendPacketToServer(
                Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
        )
    }
}