package spatialcrafting

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.AirBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.util.*

//
//private data class CrafterPieceEntityData(var multiblockIn: CrafterMultiblock? = null, var masterEntityPos: BlockPos? = null) {
//
//
//    fun addToTag(tag: CompoundTag, isMaster: Boolean) {
//        if (isMaster) {
//            multiblockIn?.addToTag(tag, Keys.multiblock)
//        }
//
//        if (masterEntityPos != null) tag.putBlockPos(masterEntityPos, Keys.masterEntity)
//    }
//
//    companion object {
//        private object Keys {
//            const val multiblock = "multiblock"
//            const val masterEntity = "master"
//        }
//
//        fun fromTag(tag: CompoundTag, pos: BlockPos) = CrafterPieceEntityData().apply {
//            masterEntityPos = tag.getBlockPos(Keys.masterEntity)
//            val isMaster =
//            if (isMaster) {
//                multiblockIn = tag.getCrafterMultiblock(key = Keys.multiblock, blockAmountInMultiblock = totalBlockAmountInMultiblock)
//            }
//        }
//    }
//}

val CrafterBlockEntityType = Builders.blockEntityType(craftersPieces) { CrafterPieceEntity() }

class CrafterPieceEntity : BlockEntity(CrafterBlockEntityType), BlockEntityClientSerializable {
    companion object {


        fun assignMultiblockState(world: World, masterPos: BlockPos, multiblock: CrafterMultiblock) {
            for (crafterEntity in multiblock.getCrafterEntities(world)) {
                with(crafterEntity) {
                    setMasterEntityPos(masterPos)
                    if (isMaster) setMultiblockIn(multiblock)
                }
            }
        }

        fun unassignMultiblockState(world: World, multiblock: CrafterMultiblock) {
            // The block which was destroyed will give a null block entity, so we need to ignore it.
            multiblock.locations.mapNotNull {
                world.getBlockEntity(it)
            }.forEach { crafterEntity ->
                with(crafterEntity.assertIs<CrafterPieceEntity>()) {
                    setMasterEntityPos(null)
                    if (isMaster) setMultiblockIn(null)
                }
            }
        }

        private object Keys {
            const val multiblock = "multiblock"
            const val masterEntity = "master"
        }
    }


    override fun toClientTag(tag: CompoundTag): CompoundTag = this.toTag(tag)

    override fun fromClientTag(tag: CompoundTag) = this.fromTag(tag)

    fun setMultiblockIn(multiblock: CrafterMultiblock?) {
        assert(!isMaster) { "Only the master should be assigned the multiblock" }
        this.multiblockIn = multiblock
        markDirty()
    }

    private fun setMasterEntityPos(pos: BlockPos?) {
        this.masterEntityPos = pos
        markDirty()
    }

    /**
     * Setting directly should only be done with the setter method
     */
    var multiblockIn: CrafterMultiblock? = null
        get() = when {
            masterEntityPos == null -> null
            isMaster -> field
            else -> masterEntity?.multiblockIn
        }
        private set

    /**
     * This is the northern-eastern most block
     *
     * Setting directly should only be done with the setter method
     */
    private var masterEntityPos: BlockPos? = null


    /**
     * When part of a multiblock, one of the pieces must be a master that stores the multiblock information
     */
    private val masterEntity: CrafterPieceEntity?
        get() = masterEntityPos?.let { masterPos ->
            if (isMaster) this
            else world?.getCrafterEntity(masterPos)
        }

    /**
     * Must be gotten AFTER [masterEntityPos] is loaded
     */
    private val isMaster: Boolean
        get() = masterEntityPos == pos


    // Serialize the BlockEntity
    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        if (isMaster) {
            tag.put(Keys.multiblock, multiblockIn?.toTag())
        }

        if (masterEntityPos != null) tag.putBlockPos(masterEntityPos, Keys.masterEntity)
        return tag

    }


    // Deserialize the BlockEntity
    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        masterEntityPos = tag.getBlockPos(Keys.masterEntity)
        if (isMaster) {
            multiblockIn = tag.toCrafterMultiblock(key = Keys.multiblock)
        }


        logDebug{"Loading CrafterPieceEntity at pos ${pos.xz}. MasterPos = ${masterEntityPos?.xz}"}
    }

}

