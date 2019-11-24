package spatialcrafting

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.client.keybinding.RecipeCreatorKeyBinding
import spatialcrafting.client.keybinding.SpatialCraftingKeyBindingCategory
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
import spatialcrafting.util.*

//TODO: things not working:
// crafting particles

//TODO: power consumption and example
//TODO: config file: can store energy


const val ModId = "spatialcrafting"


const val MaxCrafterSize = 5
const val SmallestCrafterSize = 2

val SpatialCraftingItemGroup: ItemGroup = FabricItemGroupBuilder.build(
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


    registerC2SPackets(
            Packets.StartRecipeHelp.serializer(),
            Packets.AutoCraft.serializer(),
            Packets.ChangeActiveLayer.serializer(),
            Packets.StopRecipeHelp.serializer()
    )
}


@Suppress("unused")
fun initClient() = initClientOnly(ModId) {

    HologramBlock.setRenderLayer(RenderLayer.getTranslucent())

    registerBlockModel(HologramId, HologramBakedModel.Texture) { HologramBakedModel() }

    registerS2CPackets(
            Packets.AssignMultiblockState.serializer(),
            Packets.UnassignMultiblockState.serializer(),
            Packets.StopRecipeHelp.serializer(),
            Packets.UpdateHologramContent.serializer(),
            Packets.StartCraftingParticles.serializer(),
            Packets.ItemMovementFromPlayerToMultiblockParticles.serializer(),
            Packets.StopCraftingParticles.serializer()
    )

    registerBlockEntityRenderer(HologramBlockEntity.Type, HologramBlockEntityRenderer)
    registerKeyBinding(RecipeCreatorKeyBinding)
    registerKeyBindingCategory(SpatialCraftingKeyBindingCategory)

}


