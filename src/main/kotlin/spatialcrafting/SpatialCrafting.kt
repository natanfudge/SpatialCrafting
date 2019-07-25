package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.Identifier
import spatialcrafting.util.ModInitializationContext
import spatialcrafting.util.initializeMod
import spatialcrafting.util.itemStack

//TODO: holograms
//TODO: crafting
//TODO: sounds and particles
//TODO: power consumption
//TODO: config file
//TODO: Recipe generator GUI
//TODO: rei integration


const val ModId = "spatialcrafting"

private val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { craftersPieces[0].itemStack }
object MyBlocks{
    val MY_BLOCK_INSTANCE = MyBlock(Block.Settings.of(Material.STONE))
}


@Suppress("unused")
fun init() = initializeMod(ModId) {
    FabricItem.register("fabric_item")
    HologramBlock.registerWithBlockItem("hologram", group = SpatialCraftingItemGroup)
    for (crafterPiece in craftersPieces) {
        crafterPiece.registerWithBlockItem(id = "x${crafterPiece.size}crafter_piece",
                group = SpatialCraftingItemGroup
        )
    }
    CrafterBlockEntityType.register("crafter_piece_entity")
    HologramBlockEntityType.register("hologram_entity")
    MyBlocks.MY_BLOCK_INSTANCE.registerWithBlockItem(id = "test")
    registerServerToClientPacket(Packets.CreateMultiblock)
    registerServerToClientPacket(Packets.DestroyMultiblock)
}

fun <T : Packets.Packet> ModInitializationContext.registerServerToClientPacket(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}