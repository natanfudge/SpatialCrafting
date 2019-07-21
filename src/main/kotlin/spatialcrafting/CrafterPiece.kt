package spatialcrafting

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderLayer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.world.BlockView


class CrafterPiece(settings: Settings) : Block(settings), BlockEntityProvider{
    override fun createBlockEntity(var1: BlockView?): BlockEntity? {
        return  DemoBlockEntity()
    }

}