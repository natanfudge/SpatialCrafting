package spatialcrafting.util

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.InputUtil
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
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

typealias ClientCallback = (MinecraftClient) -> Unit

@PublishedApi
internal const val Misc = "key.categories.misc"


class KotlinKeyBindingBuilder @PublishedApi internal constructor(private val id: Identifier,
                                                                 private val code: Int,
                                                                 private val type: InputUtil.Type,
                                                                 private val category: String) {
    private var onPressStart: ClientCallback? = null
    private var onReleased: ClientCallback? = null

    fun onPressStart(callback: ClientCallback) = apply { onPressStart = callback }
    fun onReleased(callback: ClientCallback) = apply { onReleased = callback }

    @PublishedApi
    internal fun build() = KotlinKeyBinding(id, code, type, category, onPressStart, onReleased)
}

class KotlinKeyBinding(id: Identifier, code: Int, type: InputUtil.Type, category: String,
                       private val onPressStart: ClientCallback?, private val onReleased: ClientCallback?)
    : FabricKeyBinding(id, type, code, category) {

    companion object {
        inline fun create(id: Identifier,
                          code: Int,
                          type: InputUtil.Type = InputUtil.Type.KEYSYM,
                          category: String = Misc, init: KotlinKeyBindingBuilder.() -> Unit = {}) = KotlinKeyBindingBuilder(
                id, code, type, category
        ).apply(init).build()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        if (onPressStart != null && pressed && !wasPressed()) {
            onPressStart.invoke(getMinecraftClient())
        }
        if (onReleased != null && !pressed && wasPressed()) {
            onReleased.invoke(getMinecraftClient())
        }
    }

}