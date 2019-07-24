package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.BlockView
import spatialcrafting.util.ModInitializationContext
import spatialcrafting.util.initializeMod
import spatialcrafting.util.itemStack

const val ModId = "spatialcrafting"

val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) {
    //TODO: ask why this is a lambda
    craftersPieces[0].itemStack
}

//class X : Block {
//    override fun buildTooltip(itemStack_1: ItemStack?, blockView_1: BlockView?, list_1: MutableList<Text>?, tooltipContext_1: TooltipContext?) {
//        super.buildTooltip(itemStack_1, blockView_1, list_1, tooltipContext_1)
//    }
//}

fun syncNoLongerMultiblock(context: PacketContext, multiblock: CrafterMultiblock) {

}


//object Packets {
//    val noLongerMultiblockPacket = id("sync_crafter_piece_removed")
//    val syncCrafterPiecePlaced = id("sync_crafter_piece_placed")
//    val test = id("test")
//}

fun id(identifier: String): Identifier {
    return Identifier(ModId, identifier)
}

@Suppress("unused")
fun init() = initializeMod(ModId) {
    FabricItem.register("fabric_item")
    for (crafterPiece in craftersPieces) {
        crafterPiece.registerWithBlockItem(id = "x${crafterPiece.size}crafter_piece",
                group = SpatialCraftingItemGroup
        )
    }
    CrafterBlockEntityType.register("crafter_piece_entity")
    registerServerToClientPacket(Packets.AssignMultiblockState)
    registerServerToClientPacket(Packets.UnassignMultiblockState)
}

fun <T : Packets.Packet> ModInitializationContext.registerServerToClientPacket(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}