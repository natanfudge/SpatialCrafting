package spatialcrafting.hologram

import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import spatialcrafting.util.kotlinwrappers.GL
import kotlin.math.sin


object HologramBlockEntityRenderer : BlockEntityRenderer<HologramBlockEntity>() {

    override fun render(tile: HologramBlockEntity, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        if(tile.contentsAreTravelling) return
        val stack = tile.getItem()
        if (!stack.isEmpty) {
            GL.begin {
                val time = tile.world!!.time + partialTicks

                // Changes the position of the item to float up and down like a sine wave.
                val offset = sin((time - tile.lastChangeTime) * OFFSET_CHANGE_SPEED_MULTIPLIER) * OFFSET_AMOUNT_MULTIPLIER
                translate(x + MOVE_TO_MID_BLOCK_OFFSET, y + offset + HEIGHT_INCREASE, z + MOVE_TO_MID_BLOCK_OFFSET)

                // Makes the item bigger
                scale(SIZE_MULTIPLIER, SIZE_MULTIPLIER, SIZE_MULTIPLIER) {
                    // Spins the item around
                    rotate(time, 0, SPIN_SPEED, 0)
                    minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND)
                }

            }

        }
    }

    //    companion object {
//        private const val ALPHA_FUNC_REF = 0.1f
    private const val OFFSET_AMOUNT_MULTIPLIER = 0.05
    private const val OFFSET_CHANGE_SPEED_MULTIPLIER = 0.125
    private const val MOVE_TO_MID_BLOCK_OFFSET = 0.5
    private const val HEIGHT_INCREASE = 0.3
    private const val SIZE_MULTIPLIER = 2
    private const val SPIN_SPEED = 1
//    }


}
