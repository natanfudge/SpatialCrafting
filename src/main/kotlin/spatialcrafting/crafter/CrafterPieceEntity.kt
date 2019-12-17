package spatialcrafting.crafter

import drawer.getFrom
import fabricktx.api.*
import kotlinx.serialization.internal.nullable
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import spatialcrafting.logDebug


class CrafterPieceEntity : KBlockEntity(CrafterPieceBlock.All), BlockEntityClientSerializable {


    companion object {

        fun assignMultiblockState(world: World, anyCrafterPos: BlockPos, multiblock: CrafterMultiblock) {
            for (crafterEntity in multiblock.getCrafterEntities(world)) {
                with(crafterEntity) {
                    setMasterEntityPos(anyCrafterPos)
                    if (isMaster) setMultiblockIn(multiblock)
                }
            }
        }

        fun unassignMultiblockState(world: World, multiblock: CrafterMultiblock) {
            // The block which was destroyed will give a null block entity, so we need to ignore it.
            multiblock.crafterLocations.mapNotNull {
                world.getBlockEntity(it) as? CrafterPieceEntity
            }.forEach { crafterEntity ->
                crafterEntity.setMasterEntityPos(null)
                if (crafterEntity.isMaster) crafterEntity.setMultiblockIn(null)
            }
        }

        private object Keys {
            const val masterEntity = "master"
        }
    }


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
            else world?.getCrafterEntityOrNull(masterPos)
        }

    /**
     * Must be gotten AFTER [masterEntityPos] is loaded
     */
    private val isMaster: Boolean
        get() = masterEntityPos == pos


    override fun toClientTag(tag: CompoundTag): CompoundTag = this.toTag(tag)

    override fun fromClientTag(tag: CompoundTag) = this.fromTag(tag)

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        if (isMaster) {
            multiblockIn?.putIn(tag)
        }

        if (masterEntityPos != null) tag.putBlockPos(Keys.masterEntity,masterEntityPos)
        return tag

    }


    // Deserialize the BlockEntity
    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        masterEntityPos = tag.getBlockPos(Keys.masterEntity)
        // Minecraft gives BlockPos(0,0,0) when there is no pos with that key.
        if (masterEntityPos == BlockPos(0, 0, 0)) masterEntityPos = null
        if (isMaster) {
            multiblockIn = CrafterMultiblock.serializer().nullable.getFrom(tag)
            // If this is true it means something went wrong with the data
            if(multiblockIn?.crafterLocations?.size != multiblockIn?.multiblockSize?.squared()){
                multiblockIn = null
            }
        }


        logDebug { "Loading CrafterPieceEntity at pos ${pos.xz}. MasterPos = ${masterEntityPos?.xz}" }
    }

}




