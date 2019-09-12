package spatialcrafting.hologram

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.item.ItemStack
import spatialcrafting.util.GL
import spatialcrafting.util.d
import kotlin.math.sin


object HologramBlockEntityRenderer : BlockEntityRenderer<HologramBlockEntity>() {

    override fun render(tile: HologramBlockEntity, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int) {
        if (tile.contentsAreTravelling) return
        val stack = tile.getItem()
        if (!stack.isEmpty) {
            GL.begin {
                val time = tile.world!!.time + partialTicks

                // Changes the position of the item to float up and down like a sine wave.
                val offset = sin((time - tile.lastChangeTime) * OffsetChangeSpeedMultiplier) * OffsetAmountMultiplier

                val targetX = x + MoveToMidBlockOffset
                val targetY = y + offset + HeightIncrease
                val targetZ = z + MoveToMidBlockOffset

                if (tile.craftingItemMovement == null) {
                    translate(targetX, targetY, targetZ)
                    // Makes the item bigger
                    scale(SizeMultiplier, SizeMultiplier, SizeMultiplier) {
                        rotate(angle = time * SpinSpeed, x = 0, y = 1, z = 0)
                        minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND)
                    }
                }
                else {
                    renderCraftingItemMovementAnimation(tile, targetX, targetY, targetZ, tile.craftingItemMovement!!, time, stack)
                }


            }

        }
    }

    private fun GL.renderCraftingItemMovementAnimation(tile: HologramBlockEntity,
                                                       targetX: Double, targetY: Double, targetZ: Double,
                                                       movementData: CraftingItemMovementData, time: Float, stack: ItemStack) {
        val craftingTargetLocation = movementData.targetLocation
        val portionOfTimePassed = (world.time - movementData.startTime).d / (movementData.endTime - movementData.startTime)
        val portionOfTimeLeft = 1 - portionOfTimePassed
        if (portionOfTimeLeft > 0) {
            val relativeCraftingTargetX = craftingTargetLocation.x - BlockEntityRenderDispatcher.renderOffsetX
            val relativeCraftingTargetY = craftingTargetLocation.y - BlockEntityRenderDispatcher.renderOffsetY
            val relativeCraftingTargetZ = craftingTargetLocation.z - BlockEntityRenderDispatcher.renderOffsetZ


            // Lean more towards the target crafting location as time goes on
            val newTargetX = targetX * portionOfTimeLeft + relativeCraftingTargetX * portionOfTimePassed
            val newTargetY = targetY * portionOfTimeLeft + relativeCraftingTargetY * portionOfTimePassed
            val newTargetZ = targetZ * portionOfTimeLeft + relativeCraftingTargetZ * portionOfTimePassed

            translate(newTargetX, newTargetY, newTargetZ)

            val sizeMultiplier = (SizeMultiplier - 1) * portionOfTimeLeft + 1

            scale(sizeMultiplier, sizeMultiplier, sizeMultiplier) {
                rotate(angle = time * MaterialCraftingSpinSpeed, x = 0, y = 1, z = 0)
                minecraft.itemRenderer.renderItem(stack, ModelTransformation.Type.GROUND)
            }


        }
//        else {
//            tile.craftingItemMovement = null
//        }


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
