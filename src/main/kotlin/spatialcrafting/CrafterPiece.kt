package spatialcrafting

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderLayer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.world.BlockView

val CrafterPieceX2 = CrafterPiece()

class CrafterPiece: Block(Settings.of(Material.STONE)), BlockEntityProvider{
    override fun createBlockEntity(var1: BlockView?): BlockEntity? {
        return  DemoBlockEntity()
    }

}