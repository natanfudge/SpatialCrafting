package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.CottonScreenController
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WLabel
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.recipe.RecipeType
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText

class DramaGeneratorController(syncId: Int, playerInventory: PlayerInventory?, context: BlockContext?)
    : CottonScreenController(
        RecipeType.SMELTING, syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context)
) {
    override fun getCraftingResultSlotIndex(): Int {
        return -1 //There's no real result slot
    }

    override fun canUse(entity: PlayerEntity?): Boolean {
        return true
    }

    init {
        val rootPanel = getRootPanel() as WGridPanel
//        rootPanel.add(WLabel(TranslatableText("block.examplemod.drama_generator"), WLabel.DEFAULT_TEXT_COLOR), 0, 0)
        val inputSlot = WItemSlot.of(blockInventory, 0)
        rootPanel.add(inputSlot, 4, 1)
        rootPanel.add(createPlayerInventoryPanel(), 0, 3)

        val button = WButton(LiteralText("halddddddo"))
        button.setOnClick {
            println("Asdf")
        }
//        button.width = 50


        rootPanel.add(button,0,0)


        rootPanel.validate(this)
    }

}