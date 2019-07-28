package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.craftersPieces
import spatialcrafting.docs.ExampleBlock
import spatialcrafting.docs.DemoBlockEntity
import spatialcrafting.docs.MyBlockEntityRenderer
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.kotlinwrappers.Builders
import spatialcrafting.util.kotlinwrappers.ModInitializationContext
import spatialcrafting.util.kotlinwrappers.ModInit
import spatialcrafting.util.kotlinwrappers.itemStack
//TODO: crafter recipe
//TODO: holograms
//TODO: crafting
//TODO: sounds and particles
//TODO: power consumption
//TODO: config file
//TODO: Recipe generator GUI
//TODO: rei integration

object MyMod {
    val MY_BLOCK = ExampleBlock()
    val MyBlockEntityType = Builders.blockEntityType(MyMod.MY_BLOCK) { DemoBlockEntity() }
}

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

    registering(Registry.RECIPE_SERIALIZER){
        SpatialRecipe.Serializer named "shaped"
    }

    registering(Registry.RECIPE_TYPE){
        SpatialRecipe.Type named SpatialRecipe.Type.Id
    }

    register(HologramBlockEntityRenderer)

    register(Packets.CreateMultiblock)
    register(Packets.DestroyMultiblock)
    register(Packets.UpdateHologramContent)


    //TODO: remove this after writing docs
    Registry.register(Registry.BLOCK, "tutorial:example_block", MyMod.MY_BLOCK)
    Registry.ITEM.add(Identifier("tutorial", "example_block"),
            BlockItem(MyMod.MY_BLOCK, Item.Settings().group(SpatialCraftingItemGroup)))
    Registry.register(Registry.BLOCK_ENTITY, "tutorial:example_block_entity", MyMod.MyBlockEntityType)

    register(MyBlockEntityRenderer())

//    LootTableLoadingCallback
//    MyBlockEntityRenderer.register()


}

//fun <S : RecipeSerializer<T>, T : Recipe<*>> register(string_1: String, recipeSerializer_1: S): S {
//    return Registry.register(Registry.RECIPE_SERIALIZER, string_1, recipeSerializer_1) as RecipeSerializer<*>
//}

fun <T : Packets.Packet> ModInitializationContext.register(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}