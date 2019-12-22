package spatialcrafting

import fabricktx.api.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import spatialcrafting.client.Sounds
import spatialcrafting.client.keybinding.MinimizeHologramsKeyBinding
import spatialcrafting.client.keybinding.RecipeCreatorKeyBinding
import spatialcrafting.client.keybinding.SpatialCraftingKeyBindingCategory
import spatialcrafting.crafter.CrafterPieceBlock
import spatialcrafting.hologram.HologramBakedModel
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramBlockEntityRenderer
import spatialcrafting.item.DeceptivelySmallSword
import spatialcrafting.item.PointyStick
import spatialcrafting.item.ShapelessSword
import spatialcrafting.recipe.*
import kotlin.math.max

//TODO:  crafting particles not working:
//TODO: power consumption and example
//TODO: config file: can store energy


const val ModId = "spatialcrafting"


const val MaxCrafterSize = 5
const val SmallestCrafterSize = 2

val SpatialCraftingItemGroup: ItemGroup = FabricItemGroupBuilder.build(
        Identifier(ModId, "spatial_crafting")
) { ItemStack(CrafterPieceBlock.ofSize(SmallestCrafterSize)) }


fun modId(str: String) = Identifier(ModId, str)

private const val HologramId = "hologram"

val Logger = Logger(
        name = "Spatial Crafting"
)

inline fun logDebug(log: () -> String) = Logger.debug(log)
inline fun logInfo(log: () -> String) = Logger.info(log)
inline fun logWarning(log: () -> String) = Logger.warning(log)

inline fun assert(message: String? = null, function: () -> Boolean) {
    if (!function()) throw AssertionError(message)
}

class SpatialCraftingInit : ModInitializer {
    override fun onInitialize() = initCommon(ModId, group = SpatialCraftingItemGroup) {
        registerBlocks {
            CrafterPieceBlock.All.withId { "x${it.size}crafter_piece" }
            HologramBlock.withId(HologramId, registerItem = false)
        }

        registerTo(Registry.ITEM) {
            ShapelessSword withId "shapeless_sword"
            DeceptivelySmallSword withId "deceptively_small_sword"
            PointyStick withId "pointy_stick"
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

}

class SpatialCraftingClientInit : ClientModInitializer {
    override fun onInitializeClient() = initClientOnly(ModId) {
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

        registerBlockEntityRenderer(HologramBlock.blockEntityType) {
            HologramBlockEntityRenderer(it)
        }

        registerKeyBindingCategory(SpatialCraftingKeyBindingCategory)
        registerKeyBinding(RecipeCreatorKeyBinding)
        registerKeyBinding(MinimizeHologramsKeyBinding)
    }

}


