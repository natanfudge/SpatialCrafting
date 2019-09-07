package spatialcrafting.crafter

import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import spatialcrafting.GuiId
import spatialcrafting.util.Builders


object TestBlock : Block(Settings.of(Material.STONE)), BlockEntityProvider {
    override fun createBlockEntity(var1: BlockView?): BlockEntity? = TestBlockEntity()

    override fun activate(blockState_1: BlockState?, world: World, pos: BlockPos, player: PlayerEntity?, hand_1: Hand?, blockHitResult_1: BlockHitResult?): Boolean {
         if (world.isClient) return true

        val be = world.getBlockEntity(pos)
        if (be != null && be is TestBlockEntity) {
            ContainerProviderRegistry.INSTANCE.openContainer(GuiId, player) { buf->
                buf.writeBlockPos(pos);
            }
        }
        return true
    }
}

class TestBlockEntity : BlockEntity(Type) {
    companion object {
        val Type = Builders.blockEntityType(TestBlock) { TestBlockEntity() }
    }
}