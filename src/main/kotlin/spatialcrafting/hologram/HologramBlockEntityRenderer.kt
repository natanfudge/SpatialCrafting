package spatialcrafting.hologram

import fabricktx.api.GL
import fabricktx.api.d
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import spatialcrafting.client.keybinding.MinimizeHologramsKeyBinding
import kotlin.math.sin


class HologramBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher?) : BlockEntityRenderer<HologramBlockEntity>(dispatcher) {
    override fun render(tile: HologramBlockEntity, partialTicks: Float, matrixStack: MatrixStack,
                        vertexConsumerProvider: VertexConsumerProvider, i: Int, j: Int) {
        if (tile.contentsAreTravelling) return
        val stack = tile.getItem()
        if (!stack.isEmpty) {
            GL.begin(matrixStack) {

                val time = tile.world!!.time + partialTicks

                val offsetAmountMultiplier = if (MinimizeHologramsKeyBinding.isPressed) {
                    MinimizedOffsetAmountMultiplier
                } else OffsetAmountMultiplier
                // Changes the position of the item to float up and down like a sine wave.
                val offset = sin((time - tile.lastChangeTime) * OffsetChangeSpeedMultiplier) * offsetAmountMultiplier

                val heightIncrease = if (MinimizeHologramsKeyBinding.isPressed) MinimizedHeightIncrease else HeightIncrease
                val targetX = MoveToMidBlockOffset
                val targetY = offset + heightIncrease
                val targetZ = MoveToMidBlockOffset

                if (tile.craftingItemMovement == null) {
                    translate(targetX, targetY, targetZ)
                    // Makes the item bigger

                    val sizeMultiplier = if (MinimizeHologramsKeyBinding.isPressed) MinimizedSizeMultiplier else SizeMultiplier

                    scale(sizeMultiplier, sizeMultiplier, sizeMultiplier) {
                        rotateY(angle = time * SpinSpeed)
                        minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND, i, j,
                                matrixStack, vertexConsumerProvider)
                    }

                } else {
                    renderCraftingItemMovementAnimation(tile, targetX, targetY, targetZ, tile.craftingItemMovement!!,
                            time, stack, matrixStack, vertexConsumerProvider, i, j)
                }


            }

        }
    }


    private fun GL.renderCraftingItemMovementAnimation(tile: HologramBlockEntity,
                                                       targetX: Double, targetY: Double, targetZ: Double,
                                                       movementData: CraftingItemMovementData, time: Float, stack: ItemStack, matrixStack: MatrixStack,
                                                       vertexConsumerProvider: VertexConsumerProvider, i: Int, j: Int) {
        val craftingTargetLocation = movementData.targetLocation
        val portionOfTimePassed = (tile.world!!.time - movementData.startTime).d / (movementData.endTime - movementData.startTime)
        val portionOfTimeLeft = 1 - portionOfTimePassed
        if (portionOfTimeLeft > 0) {
            val relativeCraftingTargetX = craftingTargetLocation.x - tile.pos.x
            val relativeCraftingTargetY = craftingTargetLocation.y - tile.pos.y
            val relativeCraftingTargetZ = craftingTargetLocation.z - tile.pos.z


            // Lean more towards the target crafting location as time goes on
            val newTargetX = targetX * portionOfTimeLeft + relativeCraftingTargetX * portionOfTimePassed
            val newTargetY = targetY * portionOfTimeLeft + relativeCraftingTargetY * portionOfTimePassed
            val newTargetZ = targetZ * portionOfTimeLeft + relativeCraftingTargetZ * portionOfTimePassed

            translate(newTargetX, newTargetY, newTargetZ)

            val sizeMultiplier = ((SizeMultiplier - 1) * portionOfTimeLeft + 1).toFloat()

            scale(sizeMultiplier, sizeMultiplier, sizeMultiplier) {
                rotateY(angle = time * MaterialCraftingSpinSpeed)
                minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND, i, j, matrixStack, vertexConsumerProvider)
            }


        }


    }

    companion object {
        private const val OffsetAmountMultiplier = 0.05
        private const val MinimizedOffsetAmountMultiplier = 0.03
        private const val OffsetChangeSpeedMultiplier = 0.125
        private const val MoveToMidBlockOffset = 0.5
        private const val HeightIncrease = 0.3
        private const val MinimizedHeightIncrease = 0.4
        private const val SizeMultiplier = 2f
        private const val MinimizedSizeMultiplier = 1f
        private const val SpinSpeed = 1
        private const val MaterialCraftingSpinSpeed = 20
    }


}
