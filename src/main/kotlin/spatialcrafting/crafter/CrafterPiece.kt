package spatialcrafting.crafter

import alexiil.mc.lib.attributes.item.impl.SimpleFixedItemInv
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import spatialcrafting.util.*
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.item.TooltipContext
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Items
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.Vec3d
import spatialcrafting.Packets
import spatialcrafting.client.shootCraftParticle
import spatialcrafting.hologram.HologramBlock
import spatialcrafting.hologram.HologramInventoryWrapper
import spatialcrafting.recipe.SpatialRecipe
import spatialcrafting.sendPacket
import spatialcrafting.util.kotlinwrappers.*
import java.util.*


val craftersPieces = listOf(
        CrafterPiece(2),
        CrafterPiece(3),
        CrafterPiece(4),
        CrafterPiece(5)
)

fun <T> BlockEntity?.assertIs(pos: BlockPos): T {
    return (this as? T)
            ?: error("BlockEntity at location $pos is not a Crafter Piece Entity as expected.\nRather, it is '${this
                    ?: "air"}'.")
}
//
//private val ironMaterial = Builders.material(
//        materialColor = MaterialColor.WHITE,
//        isSolid = true,
//        requiresTool = true,
//
//        )

//private val crafterMaterial = mapOf(
//        2 to Material.WOOD,
//        3 to Material.STONE,
//        4 to Material.METAL,
//        5 to Material.METAL
//)


fun World.getCrafterEntity(pos: BlockPos) = world.getBlockEntity(pos).assertIs<CrafterPieceEntity>(pos)
class CrafterPiece(val size: Int) : Block(Settings.copy(
        when (size) {
            2 -> Blocks.OAK_LOG
            3 -> Blocks.STONE
            4 -> Blocks.IRON_BLOCK
            5 -> Blocks.DIAMOND_BLOCK
            else -> error("unexpected size")
        }
)), BlockEntityProvider {


    companion object {
        private fun thereIsSpaceForHolograms(world: World, multiblock: CrafterMultiblock): Boolean =
                multiblock.hologramLocations.all {
                    val isAir = world.getBlock(it) is AirBlock
                    if (isAir) true
                    else {
                        if (world.isServer) logDebug { "Refusing to create multiblock due to ${world.getBlock(it)} existing in a required hologram space $it." }
                        false
                    }
                }


        private fun CrafterMultiblock.logString() = "Size = $multiblockSize, Locations = \n" + locations.groupBy { it.x }
                .entries
                .joinToString("\n") { column -> column.value.joinToString(", ") { it.xz } }

        fun createMultiblock(world: World, masterPos: BlockPos, multiblock: CrafterMultiblock) {
            if (thereIsSpaceForHolograms(world, multiblock)) {
                logDebug { "Building multiblock. [${multiblock.logString()}]" }
                CrafterPieceEntity.assignMultiblockState(world, masterPos, multiblock)

                for (hologramPos in multiblock.hologramLocations) {
                    world.setBlock(HologramBlock, pos = hologramPos)
                }


            }
            else {
                //TODO: show an indicator that there is no space
            }

        }

        fun destroyMultiblock(world: World, multiblock: CrafterMultiblock) {
            if (world.isServer) logDebug { "Destroying multiblock. [${multiblock.logString()}]" }
            CrafterPieceEntity.unassignMultiblockState(world, multiblock)

            for (hologramPos in multiblock.hologramLocations) {
                world.setBlock(Blocks.AIR, pos = hologramPos)
            }

        }
    }

    override fun createBlockEntity(var1: BlockView?) = CrafterPieceEntity()

    override fun buildTooltip(itemstack: ItemStack, blockView: BlockView?, tooltip: MutableList<Text>, tooltipContext: TooltipContext) {
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_1", size * size, size, size))
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip_2"))
    }


    private fun World.holdsCompatibleCrafterPiece(blockPos: BlockPos) = getBlock(blockPos).let {
        it is CrafterPiece && it.size == this@CrafterPiece.size
    }


    override fun onBlockRemoved(blockState: BlockState, world: World, pos: BlockPos, blockState_2: BlockState?, boolean_1: Boolean) {
        assert(world.isServer)
        val entity = world.getCrafterEntity(pos)
        val multiblock = world.getCrafterEntity(pos).multiblockIn ?: return
        destroyMultiblockFromServer(world, multiblock)
        super.onBlockRemoved(blockState, world, pos, blockState_2, boolean_1)
    }

    private fun destroyMultiblockFromServer(world: World, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        destroyMultiblock(world, multiblock)
//        PlayerStream.watching(world,multiblock.locations[0]).sendPacket(Packets.DestroyMultiblock, Packets.DestroyMultiblock(
//                multiblock
//        ))
    }

    override fun onScheduledTick(blockState_1: BlockState?, world_1: World?, blockPos_1: BlockPos?, random_1: Random?) {
        println("done!")
    }


    override fun activate(blockState_1: BlockState, world: World, pos: BlockPos, placedBy: PlayerEntity?, hand: Hand, blockHitResult_1: BlockHitResult?): Boolean {


        if (!world.isClient && hand == Hand.MAIN_HAND) {
            placedBy.sendMessage("${pos.xz}. Formed = ${world.getCrafterEntity(pos).multiblockIn != null}")
        }
        if (world.isClient) return false
        if (hand == Hand.OFF_HAND) return false

//        GlobalScope.launch {
//            delay(1000)
//            world.setBlockState(pos.south().south(), Blocks.JUKEBOX.defaultState)
//        }
        val multiblockIn = world.getCrafterEntity(pos).multiblockIn ?: return false

        PlayerStream.watching(world.getBlockEntity(pos)).sendPacket(
                Packets.StartCraftingParticles(multiblockIn)
        )


//        val  = multiblockIn.getInventory(world)
//        world.blockTickScheduler.schedule(pos,this,20)


        val match = world.recipeManager.getFirstMatch(SpatialRecipe.Type,
                CrafterMultiblockInventoryWrapper(multiblockIn.getInventory(world)), world).orElse(null)
                ?:
                //TODO: provide feedback that it didn't complete
                return false

        //TODO: delay craft

        //TODO remove contents of holograms
        //TODO drop somewhere else, and later
//        world.dropItemStack(match.output, pos)

        return false
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        val northernEasternCrafter = getNorthernEasternCrafter(world, blockPos)
        val multiblock = attemptToFormMultiblock(world, northernEasternCrafter) ?: return

        createMultiblockFromServer(world, northernEasternCrafter, multiblock)
    }

    private fun createMultiblockFromServer(world: World, northernEasternCrafter: BlockPos, multiblock: CrafterMultiblock) {
        assert(world.isServer)
        createMultiblock(
                world = world,
                masterPos = northernEasternCrafter,
                multiblock = multiblock
        )
//        PlayerStream.watching(world,multiblock.locations[0]).sendPacket(Packets.CreateMultiblock, Packets.CreateMultiblock(
//                multiblock = multiblock,
//                masterEntityLocation = northernEasternCrafter
//        ))
    }


    private fun attemptToFormMultiblock(world: World, northernEasternCrafterPos: BlockPos): CrafterMultiblock? {
        val blocks = mutableListOf<BlockPos>()
        for ((westDistance, southDistance) in (0 to 0) until (size to size)) {
            val location = northernEasternCrafterPos.west(westDistance).south(southDistance)
            if (!world.holdsCompatibleCrafterPiece(location)) {
                logDebug { "Refusing to build multiblock due to ${world.getBlock(location)} existing in required position ${location.xz}" }
                return null // All nearby blocks must be crafter pieces
            }
            blocks.add(location)

        }

        return CrafterMultiblock(blocks, size)
    }


    private fun getNorthernEasternCrafter(world: World, blockPos: BlockPos): BlockPos {
        var currentBlock = blockPos
        // Only go as far as size - 1 blocks away
        repeat(size - 1) {
            with(world) {
                val northernBlock = currentBlock.north()
                val easternBlock = currentBlock.east()
                val northernEasternBlock = currentBlock.north().east()

                when {
                    holdsCompatibleCrafterPiece(northernEasternBlock) -> currentBlock = northernEasternBlock
                    holdsCompatibleCrafterPiece(northernBlock) -> currentBlock = northernBlock
                    holdsCompatibleCrafterPiece(easternBlock) -> currentBlock = easternBlock
                }

            }
        }
        return currentBlock
    }


}

