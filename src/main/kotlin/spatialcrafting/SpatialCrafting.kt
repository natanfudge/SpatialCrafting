package spatialcrafting

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.client.gui.DramaGeneratorController
import spatialcrafting.client.gui.DramaGeneratorScreen
//import spatialcrafting.client.gui.DramaGeneratorController
//import spatialcrafting.client.gui.DramaGeneratorScreen
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.CraftersPieces
//import spatialcrafting.docs.ExampleMod.docsJavaCommonInit
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.recipe.ShapedSpatialRecipe
import spatialcrafting.recipe.ShapelessSpatialRecipe
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.util.kotlinwrappers.initClientOnly
import spatialcrafting.util.kotlinwrappers.initCommon
import spatialcrafting.util.kotlinwrappers.itemStack

//TODO: fix sounds tutorial (can't reuse the id field...)
//TODO: fix BER tutorial (needs to be on client)

//TODO: test EVERYTHING on server
//TODO: test itemstack
//TODO: better x5 crafter texture

//TODO: document packets:
// - S2C -  Need to do taskQueue thing, needs to be registered on the client only
// - C2S - Need to do taskQueue thing and beware of vulns (isLoaded, etc)
//TODO: document recipes (remember that you need to implement read and write properly...)
//TODO: rei integration
//TODO: better example recipes (some op items - sword of you want to craft this etc)
//TODO: test putting items in differnet locations in large crafter with small recipe

//TODO: ask to add to AOF
//TODO: add dependencies in fabric.mod.json
//TODO: power consumption
//TODO: config file: sounds power multiplier, can store energy
//TODO: Recipe generator GUI
//TODO: remove useless shit for tutorials and such

const val ModId = "spatialcrafting"

val GuiId = modId("test_gui")

const val MaxCrafterSize = 5
const val MinCrafterSize = 2

private val SpatialCraftingItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { CraftersPieces.getValue(MinCrafterSize).itemStack }

fun modId(str: String) = Identifier(ModId, str)


@Suppress("unused")
fun init() = initCommon(ModId, group = SpatialCraftingItemGroup) {

    registerBlocksWithItemBlocks {
        for (crafterPiece in CraftersPieces.values) {
            crafterPiece withId "x${crafterPiece.size}crafter_piece"
        }
    }

    registerTo(Registry.BLOCK) {
        HologramBlock withId "hologram"
    }

    registerTo(Registry.BLOCK_ENTITY) {
        CrafterPieceEntity.Type withId "crafter_piece_entity"
        HologramBlockEntity.Type withId "hologram_entity"
    }

    registerTo(Registry.RECIPE_SERIALIZER) {
        ShapedSpatialRecipe.Serializer withId "shaped"
        ShapelessSpatialRecipe withId "shapeless"
    }

    registerTo(Registry.RECIPE_TYPE) {
        SpatialRecipe.Type withId SpatialRecipe.Type.Id
    }
//
    registerTo(Registry.SOUND_EVENT) {
        Sounds.CraftEnd withId Sounds.CraftEndId
        Sounds.CraftLoop withId Sounds.CraftLoopId
        Sounds.CraftStart withId Sounds.CraftStartId
    }
//
//
    registerC2S(Packets.StartRecipeHelp.serializer())
    registerC2S(Packets.StopRecipeHelp.serializer())

    ContainerProviderRegistry.INSTANCE.registerFactory(GuiId) { syncId, _, player, buf ->
        DramaGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
    }

//    docsJavaCommonInit()


}

@Suppress("unused")
fun initClient() = initClientOnly(ModId) {
    registerS2C(Packets.AssignMultiblockState.serializer())
    registerS2C(Packets.UnassignMultiblockState.serializer())

    registerS2C(Packets.UpdateHologramContent.serializer())
    registerS2C(Packets.StartCraftingParticles.serializer())

    register(HologramBlockEntityRenderer)

    ScreenProviderRegistry.INSTANCE.registerFactory(GuiId) { syncId, _, player, buf ->
        DramaGeneratorScreen(
                DramaGeneratorController(
                        syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())
                ), player
        )
    }

}


