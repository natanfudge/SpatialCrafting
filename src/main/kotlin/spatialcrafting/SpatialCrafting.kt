package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.container.BlockContext
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.client.gui.DramaGeneratorController
import spatialcrafting.client.gui.DramaGeneratorScreen
//import spatialcrafting.client.gui.DramaGeneratorController
//import spatialcrafting.client.gui.DramaGeneratorScreen
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.TestBlock
import spatialcrafting.crafter.TestBlockEntity
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.docs.ExampleMod.docsJavaInit
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.recipe.ShapedSpatialRecipe
import spatialcrafting.recipe.ShapelessSpatialRecipe
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.kotlinwrappers.ModInit
import spatialcrafting.util.kotlinwrappers.ModInitializationContext
import spatialcrafting.util.kotlinwrappers.itemStack


//TODO: remember to handle changes in state in kapt project
//TODO: remember to test nullable values in kapt project


//TODO: document recipes
//TODO: rei integration
//TODO: better example recipes (some op items)
//TODO: test putting items in differnet locations in large crafter with small recipe
//TODO: test on server
//TODO: ask to add to AOF
//TODO: add dependencies in fabric.mod.json
//TODO: power consumption
//TODO: config file: sounds power multiplier, can store energy
//TODO: Recipe generator GUI


const val ModId = "spatialcrafting"

 val GuiId = id("test_gui")

const val MaxCrafterSize = 5
const val MinCrafterSize = 2

private val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { CraftersPieces[MinCrafterSize]!!.itemStack }

fun id(str: String) = Identifier(ModId, str)


//TODO: ListTag.value -> ListTag.tags
@Suppress("unused")
fun init() = ModInit.begin(ModId, group = SpatialCraftingItemGroup) {

    registeringWithItemBlocks {
        for (crafterPiece in CraftersPieces.values) {
            crafterPiece withId "x${crafterPiece.size}crafter_piece"
        }
//        TestBlock withId "test_block"
    }

    registerTo(Registry.BLOCK) {
        HologramBlock withId "hologram"
    }

    registerTo(Registry.BLOCK_ENTITY) {
        CrafterPieceEntity.Type withId "crafter_piece_entity"
        HologramBlockEntity.Type withId "hologram_entity"
//        TestBlockEntity.Type withId "test_block_entity"
    }

    registerTo(Registry.RECIPE_SERIALIZER) {
        ShapedSpatialRecipe.Serializer withId "shaped"
        ShapelessSpatialRecipe withId "shapeless"
    }

    registerTo(Registry.RECIPE_TYPE) {
        SpatialRecipe.Type withId SpatialRecipe.Type.Id
    }

//    Registry.register(Registry.SOUND_EVENT, Identifier("spatialcrafting", "craft_end"), TestSoundEvent)
//    register("spatialcrafting:craft_end")


    registerTo(Registry.SOUND_EVENT) {
        Sounds.CraftEnd withId Sounds.CraftEnd.id.path
        Sounds.CraftLoop withId Sounds.CraftLoop.id.path
        Sounds.CraftStart withId Sounds.CraftStart.id.path
    }

    register(HologramBlockEntityRenderer)

    register(Packets.AssignMultiblockState)
    register(Packets.UnassignMultiblockState)

    register(Packets.UpdateHologramContent)
    register(Packets.StartCraftingParticles)
//    register(Packets.CancelCraftingParticles)

    ContainerProviderRegistry.INSTANCE.registerFactory(GuiId) { syncId, _, player, buf ->
        DramaGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
    }

    docsJavaInit()


}

@Suppress("unused")
fun initClient(){
    ScreenProviderRegistry.INSTANCE.registerFactory(GuiId) { syncId, _, player, buf->
        DramaGeneratorScreen(
                DramaGeneratorController(
                        syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())
                ), player
        )
    }

}





fun <T : Packets.Packet<T>> ModInitializationContext.register(manager: Packets.PacketManager<T>) {
    registerServerToClientPacket(manager.id) { packetContext, packetByteBuf ->
        manager.use(packetContext, manager.fromBuf(packetByteBuf))
    }
}