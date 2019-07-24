package spatialcrafting

import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import spatialcrafting.util.*
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.item.TooltipContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText


val craftersPieces = listOf(
        CrafterPiece(2),
        CrafterPiece(3),
        CrafterPiece(4),
        CrafterPiece(5)
)

fun BlockEntity?.assertIsCrafterBE(): CrafterPieceEntity {
//    contract {
//        returns() implies (this@assertIsCrafterBE is CrafterPieceEntity)
//    }
    if (this !is CrafterPieceEntity) error("BlockEntity at location ${this?.pos} is not a Crafter Piece Entity as expected.\nRather, it is '$this'.")
    return this
}

private fun World.getCrafterEntity(pos: BlockPos) = world.getBlockEntity(pos).assertIsCrafterBE()
class CrafterPiece(val size: Int) : Block(Settings.of(Material.STONE)), BlockEntityProvider {

    override fun createBlockEntity(var1: BlockView?) = CrafterPieceEntity()

    override fun buildTooltip(itemstack: ItemStack?, blockView: BlockView?, tooltip: MutableList<Text>, tooltipContext: TooltipContext?) {
        tooltip.add(TranslatableText("block.spatialcrafting.crafter_piece.tooltip", size * size, size, size))
    }


    private fun World.holdsCompatibleCrafterPiece(blockPos: BlockPos) = getBlockAtLocation(blockPos).let {
        it is CrafterPiece && it.size == this@CrafterPiece.size
    }


    override fun onBlockRemoved(blockState: BlockState, world: World, pos: BlockPos, blockState_2: BlockState?, boolean_1: Boolean) {
        assert(world.isServer)
        val multiblock = world.getCrafterEntity(pos).multiblockIn ?: return
        logMultiblockDestruction(multiblock)
        unformMultiblock(world, multiblock)
    }

    private fun unformMultiblock(world: World, multiblock: CrafterMultiblock) {
        CrafterPieceEntity.unassignMultiblockState(world, multiblock)
        PlayerStream.all(world.server).sendPacket(Packets.UnassignMultiblockState, Packets.UnassignMultiblockState(
                multiblock
        ))
    }


    override fun activate(blockState_1: BlockState, world: World, pos: BlockPos, placedBy: PlayerEntity?, hand: Hand?, blockHitResult_1: BlockHitResult?): Boolean {
        if (!world.isClient && hand == Hand.MAIN_HAND) {
            placedBy.sendMessage("${pos.xz}. Formed = ${world.getCrafterEntity(pos).multiblockIn != null}")
        }

        return false
    }


    override fun onPlaced(world: World, blockPos: BlockPos, blockState: BlockState, placedBy: LivingEntity?, itemStack: ItemStack?) {
        if (world.isClient) return
        val northernEasternCrafter = getNorthernEasternCrafter(world, blockPos)
        val multiblock = attemptToFormMultiblock(world, northernEasternCrafter) ?: return
        logMultiblockCreation(multiblock)
        formMultiblock(world, northernEasternCrafter, multiblock)
    }

    private fun formMultiblock(world: World, northernEasternCrafter: BlockPos, multiblock: CrafterMultiblock) {
        CrafterPieceEntity.assignMultiblockState(
                world = world,
                masterPos = northernEasternCrafter,
                multiblock = multiblock
        )
        PlayerStream.all(world.server).sendPacket(Packets.AssignMultiblockState, Packets.AssignMultiblockState(
                multiblock = multiblock,
                masterEntityLocation = northernEasternCrafter
        ))
    }


    private fun attemptToFormMultiblock(world: World, northernEasternCrafterPos: BlockPos): CrafterMultiblock? {
        val blocks = mutableListOf<BlockPos>()
        for ((westDistance, southDistance) in (0 to 0) until (size to size)) {
            val location = northernEasternCrafterPos.west(westDistance).south(southDistance)
            if (!world.holdsCompatibleCrafterPiece(location)) {
                logDebug("Refusing to build multiblock due to ${world.getBlockAtLocation(location)} existing in required position ${location.xz}")
                return null // All nearby blocks must be crafter pieces
            }
            blocks.add(location)

        }

        return CrafterMultiblock(blocks, size)
    }

    private fun logMultiblockCreation(multiblock: CrafterMultiblock) {
        logDebug("Building multiblock. Locations:\n [${multiblock.logString()}]")
    }

    private fun logMultiblockDestruction(multiblock: CrafterMultiblock) {
        logDebug("Destroying multiblock. Locations:\n [${multiblock.logString()}]")
    }

    private fun CrafterMultiblock.logString() = locations.groupBy { it.x }
            .entries
            .joinToString("\n") { column -> column.value.joinToString(", ") { it.xz } }

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

private const val locationKey = "location"
private const val sizeKey = "size"

data class CrafterMultiblock(
        /**
         * This is the northern-eastern most block's location.
         */
        val locations: List<BlockPos>,
        val size: Int
) : Serializable<CrafterMultiblock> {
    override fun toTag(): CompoundTag = CompoundTag().apply {
        locations.forEachIndexed { i, blockPos ->
            putBlockPos(blockPos, locationKey + i)
        }

        putInt(sizeKey, size)
    }

    fun getCrafterEntities(world: World): List<CrafterPieceEntity> = locations.map {
        world.getCrafterEntity(it)
    }


}

fun totalPieceAmount(multiblockSize: Int) = multiblockSize * multiblockSize

fun CompoundTag.toCrafterMultiblock(): CrafterMultiblock? {
    val size = getInt(sizeKey)

    val locations = (0 until totalPieceAmount(size)).mapNotNull { i ->
        getBlockPos(locationKey + i)
    }

    // If it's empty it means everything is null
    return if (locations.isEmpty()) return null
    else CrafterMultiblock(locations, size)
}

/**
 * Gets the tag with the key and then deserializes it
 */
fun CompoundTag.toCrafterMultiblock(key: String): CrafterMultiblock? {
    return this.transformCompoundTag(key) { this.toCrafterMultiblock() }
}