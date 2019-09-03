package spatialcrafting


import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.client.keybinding.RecipeCreatorKeyBinding
import spatialcrafting.crafter.CrafterPieceEntity
import spatialcrafting.crafter.CraftersPieces
import spatialcrafting.hologram.HologramBakedModel
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntity
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.item.DeceptivelySmallSword
import spatialcrafting.item.PointyStick
import spatialcrafting.item.ShapelessSword
import spatialcrafting.recipe.*
import spatialcrafting.util.itemStack
import spatialcrafting.util.initClientOnly
import spatialcrafting.util.initCommon
import java.util.function.Function


//TODO: need examples for:
// power



//TODO: ask to add to AOF
//TODO: power consumption
//TODO: config file: can store energy
//TODO: remove useless shit for tutorials and such, go through everything and make sure its needed
//TODO: document packets:
// - S2C -  Need to do taskQueue thing, needs to be registered on the client only
// - C2S - Need to do taskQueue thing and beware of vulns (isLoaded, etc)
//TODO: document recipes (remember that you need to implement read and write properly...)
//TODO: better example recipes (some op items - sword of you want to craft this etc)

const val ModId = "spatialcrafting"

val GuiId = modId("test_gui")

const val MaxCrafterSize = 5
const val SmallestCrafterSize = 2

val SpatialCraftingItemGroup: ItemGroup  = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { CraftersPieces.getValue(SmallestCrafterSize).itemStack }


fun modId(str: String) = Identifier(ModId, str)

private const val HologramId = "hologram"

@Suppress("unused")
fun init() = initCommon(ModId, group = SpatialCraftingItemGroup) {

    registerBlocksWithItemBlocks {
        for (crafterPiece in CraftersPieces.values) {
            crafterPiece withId "x${crafterPiece.size}crafter_piece"
        }
    }

    registerTo(Registry.ITEM) {
        ShapelessSword withId "shapeless_sword"
        DeceptivelySmallSword withId "deceptively_small_sword"
        PointyStick withId "pointy_stick"
    }

    registerTo(Registry.BLOCK) {
        HologramBlock withId HologramId
    }

    registerTo(Registry.BLOCK_ENTITY) {
        CrafterPieceEntity.Type withId "crafter_piece_entity"
        HologramBlockEntity.Type withId "hologram_entity"
    }

    registerTo(Registry.RECIPE_SERIALIZER) {
        ShapedSpatialRecipe withId Identifier(ShapedRecipeType)
        ShapelessSpatialRecipe withId Identifier(ShapelessRecipeType)
    }

    registerTo(Registry.RECIPE_TYPE) {
        SpatialRecipe.Type withId SpatialRecipe.Type.Id
    }

    registerTo(Registry.SOUND_EVENT) {
        Sounds.CraftEnd withId Sounds.CraftEndId
        Sounds.CraftLoop withId Sounds.CraftLoopId
        Sounds.CraftStart withId Sounds.CraftStartId
    }


    registerC2S(Packets.StartRecipeHelp.serializer())
    registerC2S(Packets.AutoCraft.serializer())
    registerC2S(Packets.ChangeActiveLayer.serializer())
    registerC2S(Packets.StopRecipeHelp.serializer())




}


@Suppress("unused")
fun initClient() = initClientOnly(ModId) {

    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
        ModelVariantProvider { modelId, _ ->
            if (modelId.namespace == ModId && modelId.path == HologramId) {
                object : UnbakedModel {
                    override fun bake(modelLoader: ModelLoader, spriteFunction: Function<Identifier, Sprite>, settings: ModelBakeSettings): BakedModel {
                        return HologramBakedModel()
                    }

                    override fun getModelDependencies(): List<Identifier> = listOf()
                    override fun getTextureDependencies(unbakedModelFunction: Function<Identifier, UnbakedModel>, strings: MutableSet<String>): List<Identifier> =
                            listOf(HologramBakedModel.Texture)
                }
            }
            else null
        }
    }

    registerS2C(Packets.AssignMultiblockState.serializer())
    registerS2C(Packets.UnassignMultiblockState.serializer())
    registerS2C(Packets.StopRecipeHelp.serializer())
    registerS2C(Packets.UpdateHologramContent.serializer())
    registerS2C(Packets.StartCraftingParticles.serializer())
    registerS2C(Packets.ItemMovementFromPlayerToMultiblockParticles.serializer())

    register(HologramBlockEntityRenderer)
    register(RecipeCreatorKeyBinding)

}


