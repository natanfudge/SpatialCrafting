package spatialcrafting.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos

object Client {
    fun drawCenteredStringWithoutShadow(textRenderer: TextRenderer, string: String?, x: Int, y: Int, int_3: Int) {
        textRenderer.draw(string, (x - textRenderer.getStringWidth(string) / 2).toFloat(), y.toFloat(), int_3)
    }

    fun playButtonClickSound() = getMinecraftClient().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))

    private const val RerenderFlag = 8


    fun scheduleRenderUpdate(pos: BlockPos) = getMinecraftClient().worldRenderer.updateBlock(
            null, pos, null, null, RerenderFlag
    )
}


fun getMinecraftClient(): MinecraftClient = MinecraftClient.getInstance()

