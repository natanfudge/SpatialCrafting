package spatialcrafting.hologram

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import spatialcrafting.util.GL
import spatialcrafting.util.d
import kotlin.math.sin


object HologramBlockEntityRenderer : BlockEntityRenderer<HologramBlockEntity>(BlockEntityRenderDispatcher.INSTANCE) {
    override fun render(tile: HologramBlockEntity, partialTicks: Float, matrixStack: MatrixStack,
                        vertexConsumerProvider: VertexConsumerProvider, i: Int, j: Int) {
        if (tile.contentsAreTravelling) return
        val stack = tile.getItem()
        if (!stack.isEmpty) {
            GL.begin {
                val time = tile.world!!.time + partialTicks

                // Changes the position of the item to float up and down like a sine wave.
                val offset = sin((time - tile.lastChangeTime) * OffsetChangeSpeedMultiplier) * OffsetAmountMultiplier

                val targetX = MoveToMidBlockOffset
                val targetY = offset + HeightIncrease
                val targetZ = MoveToMidBlockOffset

                if (tile.craftingItemMovement == null) {
                    translate(targetX, targetY, targetZ)
                    // Makes the item bigger
                    scale(SizeMultiplier, SizeMultiplier, SizeMultiplier) {
                        rotate(angle = time * SpinSpeed, x = 0, y = 1, z = 0)
                        minecraft.itemRenderer.method_23178(stack, ModelTransformation.Type.GROUND, i, j, matrixStack, vertexConsumerProvider)
                    }
                } else {
                    renderCraftingItemMovementAnimation(tile, targetX, targetY, targetZ, tile.craftingItemMovement!!,
                            time, stack,matrixStack,vertexConsumerProvider, i, j)
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
            //TODO: this is probably broke
            val relativeCraftingTargetX = craftingTargetLocation.x /*- BlockEntityRenderDispatcher.renderOffsetX*/
            val relativeCraftingTargetY = craftingTargetLocation.y /*- BlockEntityRenderDispatcher.renderOffsetY*/
            val relativeCraftingTargetZ = craftingTargetLocation.z /*- BlockEntityRenderDispatcher.renderOffsetZ*/


            // Lean more towards the target crafting location as time goes on
            val newTargetX = targetX * portionOfTimeLeft + relativeCraftingTargetX * portionOfTimePassed
            val newTargetY = targetY * portionOfTimeLeft + relativeCraftingTargetY * portionOfTimePassed
            val newTargetZ = targetZ * portionOfTimeLeft + relativeCraftingTargetZ * portionOfTimePassed

            translate(newTargetX, newTargetY, newTargetZ)

            val sizeMultiplier = (SizeMultiplier - 1) * portionOfTimeLeft + 1

            scale(sizeMultiplier, sizeMultiplier, sizeMultiplier) {
                rotate(angle = time * MaterialCraftingSpinSpeed, x = 0, y = 1, z = 0)
                minecraft.itemRenderer.method_23178(stack, ModelTransformation.Type.GROUND, i, j, matrixStack, vertexConsumerProvider)
            }


        }


    }


    //FIXME: thing that pops above head when you place stuff in hologram
    private const val OffsetAmountMultiplier = 0.05
    private const val OffsetChangeSpeedMultiplier = 0.125
    private const val MoveToMidBlockOffset = 0.5
    private const val HeightIncrease = 0.3
    private const val SizeMultiplier = 2
    private const val SpinSpeed = 1
    private const val MaterialCraftingSpinSpeed = 20


}
