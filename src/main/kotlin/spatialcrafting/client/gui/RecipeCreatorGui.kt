package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import net.minecraft.item.Items
import spatialcrafting.Packets
import spatialcrafting.client.gui.widgets.*
import spatialcrafting.client.gui.widgets.core.*
import spatialcrafting.compat.rei.getNearestCrafter
import spatialcrafting.crafter.CrafterMultiblock
import spatialcrafting.crafter.RecipeCreatorCurrentLayerInactive
import spatialcrafting.sendPacketToServer
import spatialcrafting.util.getMinecraftClient
import spatialcrafting.util.playButtonClickSoundToClient

data class GeneratedRecipeState(var result: String? = null)

data class RecipeOptions(var shaped: Boolean = true,
                         var craftTime: String,
                         var energyCost: Int = 1000,
                         var minimumCrafterSize: String,
                         var useTags: Boolean = true)

class RecipeCreatorGui : LightweightGuiDescription() {
    init {
        val nearestCrafter = getNearestCrafter(getMinecraftClient().world, getMinecraftClient().player.pos)
                ?: error("Crafter GUI opened without a crafter multiblock")

        val generatedRecipeState = GeneratedRecipeState()

        val options = RecipeOptions(craftTime = (nearestCrafter.multiblockSize * 5f).toString(),
                minimumCrafterSize = nearestCrafter.multiblockSize.toString())

        drawWidgets(250, 160) {
            Row {
                UpDownButtons(nearestCrafter)

                HorizontalSpace(20)
                GenerateRecipeButton(nearestCrafter, generatedRecipeState, options)

                Column(MainAxisAlignment.Center, crossAxisAlignment = CrossAxisAlignment.Baseline) {
                    Text("Shaped")
                    VerticalSpace(1)
                    Switch(enabled = options.shaped).onClick {
                        options.shaped = !options.shaped
                        recompose(this@Column)
                    }
                    VerticalSpace(2)
                    Text("Use Tags", "Where Possible")
                    VerticalSpace(1)
                    Switch(enabled = options.useTags).onClick {
                        options.useTags = !options.useTags
                        recompose(this@Column)
                    }
                    VerticalSpace(2)
                    Text("Craft Time", "(Seconds)")
                    VerticalSpace(2)
                    TextField(description = this@RecipeCreatorGui, width = 40, defaultText = options.craftTime) {
                        options.craftTime = it
                    }
                    VerticalSpace(5)
                    Text("Minimum", "Crafter Size")
                    VerticalSpace(2)
                    TextField(description = this@RecipeCreatorGui, width = 14, defaultText = options.minimumCrafterSize) {
                        options.minimumCrafterSize = it
                    }

                }

            }


        }


    }

    private fun DevWidget.GenerateRecipeButton(nearestCrafter: CrafterMultiblock, generatedRecipeState: GeneratedRecipeState, options: RecipeOptions) {
        Column(MainAxisAlignment.Center, crossAxisAlignment = CrossAxisAlignment.Baseline, crossAxisSize = FlexSize.Expand) {
            val thereAreAnyItemsInCrafter = nearestCrafter.getInventory(getMinecraftClient().world).isNotEmpty()
            val playerHoldsItem = getMinecraftClient().player.mainHandStack.item != Items.AIR
            Button("Generate Recipe", enabled = thereAreAnyItemsInCrafter && playerHoldsItem, onClick = {
                generatedRecipeState.result = generateRecipe(nearestCrafter, options)
                recompose(this)
                getMinecraftClient().server?.reload()
            }).onHover { _, _ ->
                if (!playerHoldsItem) overlay!!.tooltip = errorText("Hold the result item in your hand")
                if (!thereAreAnyItemsInCrafter) overlay!!.tooltip = errorText("Put the input items in the crafter")
            }

            Padding(3, 3, 3, 3) {
                Row(MainAxisAlignment.Center, mainAxisSize = FlexSize.Wrap) {
                    if (generatedRecipeState.result != null) Text(generatedRecipeState.result!!)
                }
            }
        }
    }
}

private fun DevWidget.UpDownButtons(nearestCrafter: CrafterMultiblock) {
    Column(MainAxisAlignment.Center, crossAxisAlignment = CrossAxisAlignment.Baseline/*,crossAxisSize = FlexSize.Expand*/) {
        if (nearestCrafter.recipeCreatorCurrentLayer != RecipeCreatorCurrentLayerInactive) {
            Text("${nearestCrafter.recipeCreatorCurrentLayer}", color = 0x55_FF_FF)
        }

        DisableableImage(
                enabledTexture = "gui/button/up_on.png",
                disabledTexture = "gui/button/up_off.png",
                width = 13,
                height = 11,
                enabled = nearestCrafter.recipeCreatorCurrentLayer < nearestCrafter.multiblockSize - 1,
                hoverText = "Show the next hologram layer"
        ) {
            changeCurrentLayer(nearestCrafter, change = +1, recompositionTarget = this@Column)
        }

        VerticalSpace(5)
        DisableableImage(
                enabledTexture = "gui/button/down_on.png",
                disabledTexture = "gui/button/down_off.png",
                width = 13,
                height = 11,
                enabled = nearestCrafter.recipeCreatorCurrentLayer > 0,
                hoverText = "Show the previous hologram layer"
        ) {
            changeCurrentLayer(nearestCrafter, change = -1, recompositionTarget = this@Column)
        }

    }
}

private fun DevWidget.HoverableImage(path: String, width: Int, height: Int): DevWidget {
    return Image(path = path, width = width, height = height).onHover { _, _ ->
        ScreenDrawing.rect(constraints.x, constraints.y, constraints.width, constraints.height, 0x40_00_00_FF)
    }
}

private fun DevWidget.DisableableImage(enabledTexture: String,
                                       disabledTexture: String,
                                       width: Int,
                                       height: Int,
                                       enabled: Boolean,
                                       hoverText: String = "",
                                       onClick: DevWidget.(RuntimeWidget) -> Unit) =
        if (enabled) HoverableImage(enabledTexture, width, height).onClick {
            playButtonClickSoundToClient()
            onClick(it)
        }.onHover { _, _ ->
            if (hoverText != "") {
                overlay!!.tooltip = hoverText
            }
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
