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

//TODO: tooltips for indiviual crafters
//TODO: holograms
//TODO: crafting
//TODO: sounds and particles
//TODO: power consumption
//TODO: config file
//TODO: Recipe generator GUI
//TODO: rei integration


const val ModId = "spatialcrafting"

val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) {
    craftersPieces[0].itemStack
}


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