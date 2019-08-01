package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.craftersPieces
import spatialcrafting.docs.ExampleMod
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.kotlinwrappers.ModInit
import spatialcrafting.util.kotlinwrappers.ModInitializationContext
import spatialcrafting.util.kotlinwrappers.itemStack

//TODO: remember to handle changes in state in kapt project
//TODO: remember to test nullable values in kapt project


//TODO: sounds and document
//TODO: power consumption and recipe time cost
//TODO: config file: sounds power multiplier, can store energy
//TODO: Recipe generator GUI
//TODO: rei integration
//TODO: test on server


const val ModId = "spatialcrafting"


private val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { craftersPieces[0].itemStack }

fun id(str: String) = Identifier(ModId, str)


@Suppress("unused")
fun init() = ModInit.begin(ModId, group = SpatialCraftingItemGroup) {
    registering(Registry.ITEM) {
        FabricItem named "fabric_item"
    }


    registeringWithItemBlocks {
        for (crafterPiece in craftersPieces) {
            crafterPiece named "x${crafterPiece.size}crafter_piece"
        }
    }

    registering(Registry.BLOCK) {
        HologramBlock named "hologram"
    }

    registering(Registry.BLOCK_ENTITY) {
        CrafterPieceEntity.Type named "crafter_piece_entity"
        HologramBlockEntity.Type named "hologram_entity"
    }

    registering(Registry.RECIPE_SERIALIZER) {
        SpatialRecipe.Serializer named "shaped"
    }

    registering(Registry.RECIPE_TYPE) {
        SpatialRecipe.Type named SpatialRecipe.Type.Id
    }

//    Registry.register(Registry.SOUND_EVENT, Identifier("spatialcrafting", "craft_end"), TestSoundEvent)
//    register("spatialcrafting:craft_end")


    registering(Registry.SOUND_EVENT) {
        Sounds.CraftEnd named Sounds.CraftEnd.id.path
        Sounds.CraftLoop named Sounds.CraftLoop.id.path
        Sounds.CraftStart named Sounds.CraftStart.id.path
    }

    register(HologramBlockEntityRenderer)

//    register(Packets.CreateMultiblock)
//    register(Packets.DestroyMultiblock)

    register(Packets.UpdateHologramContent)
    register(Packets.StartCraftingParticles)
//    register(Packets.CancelCraftingParticles)

    ExampleMod.docsJavaInit()

}



fun <T : Packets.Packet<T>> ModInitializationContext.register(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}