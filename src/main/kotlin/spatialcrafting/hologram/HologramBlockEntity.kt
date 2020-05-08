@file:UseSerializers(ForIngredient::class)

package spatialcrafting.hologram

import alexiil.mc.lib.attributes.AttributeList
import drawer.ForIngredient
import fabricktx.api.*
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import spatialcrafting.Packets
import spatialcrafting.crafter.*

private const val TicksPerSecond = 20

/**
 * Used to determine when to put the hologram inventory when doing the material crafting 'particles'
 */
data class CraftingItemMovementData(val targetLocation: Vec3d, val startTime: Long, val endTime: Long)

class HologramBlockEntity : KBlockEntity(HologramBlock), BlockEntityClientSerializable, RenderAttachmentBlockEntity, Tickable {



    override fun getRenderAttachmentData(): Any = ghostIngredient?.let {
        if (it.matches(getItem())) ItemStack.EMPTY
        else it.matchingStacksClient[(ghostIngredientCycleIndex / TicksPerSecond) % it.matchingStacksClient.size]
    } ?: ItemStack.EMPTY


    companion object {
        fun assignMultiblockState(multiblock: CrafterMultiblock, world: World, masterEntityPos : BlockPos){
            for (hologramPos in multiblock.hologramLocations) {
                world.setBlock(HologramBlock, pos = hologramPos)
                world.getHologramEntity(hologramPos).masterEntityPos = masterEntityPos
            }
//            multiblock.hologramLocations.forEach { world.getHologramEntity(it).masterEntityPos = masterEntityPos }
        }


        private object Keys {
            const val Inventory = "inventory"
            const val LastChangeTime = "last_change_time"
            const val MasterPos = "master_pos"
        }
    }


    val inventory = HologramInventory().also {
        // Since inventory is only changed at server side we need to send a packet to the client
        // Note: this will be called again in the client after we do that, so we need to ignore it that time.
        it.setOwnerListener { _, _, previousStack, currentStack ->
            lastChangeTime = world!!.time
            if (world!!.isClient) {
                return@setOwnerListener
            }

            if (!previousStack.isItemEqual(currentStack)) {
                PlayerStream.watching(this).sendPacket(Packets.UpdateHologramContent(this.pos, currentStack))
            }
        }
    }


    val multiblockIn: CrafterMultiblock?
        get() = masterEntity?.multiblockIn

    /**
     * This is the northern-eastern most block
     */
    private var masterEntityPos: BlockPos? = null
        set(value) {
            field = value
            markDirty()
        }


    /**
     * When part of a multiblock, one of the pieces must be a master that stores the multiblock information
     */
    private val masterEntity: CrafterPieceEntity?
        get() = masterEntityPos?.let { world?.getCrafterEntityOrNull(it) }


    private val ghostIngredient: Ingredient?
        get() = multiblockIn?.hologramGhostIngredientFor(this)
                .also { ghostIngredientActive = it != null }

    /**
     * A way to avoid checking if there is a ghost ingredient every tick
     */
    private var ghostIngredientActive: Boolean = false

    /**
     * Measured in ticks and then divided by 20. Used to change the item that is showed in the ghost item constantly
     */
    private var ghostIngredientCycleIndex = 0

    /**
     * Used for [spatialcrafting.client.particle.ItemMovementParticle] so it doesn't appear that the item arrives instantly when
     * in the particle it looks like it is still travelling
     */
    var contentsAreTravelling = false


    /**
     * This is just used for client sided rendering of the block so the items in the holograms don't move in sync.
     */
    var lastChangeTime: Long = 0

    /**
     * Used to determine when to put the hologram inventory when doing the material crafting 'particles'
     * in [HologramBlockEntityRenderer]
     * (client only)
     */
    var craftingItemMovement: CraftingItemMovementData? = null

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        tag.put(Keys.Inventory, inventory.toTag())
        tag.putLong(Keys.LastChangeTime, lastChangeTime)
        tag.putBlockPos(Keys.MasterPos, masterEntityPos)
        return tag
    }

    override fun fromTag(state: BlockState?,tag: CompoundTag) {
        super.fromTag(state,tag)
        inventory.fromTag(tag.getCompound(Keys.Inventory))
        lastChangeTime = tag.getLong(Keys.LastChangeTime)
        masterEntityPos = tag.getBlockPos(Keys.MasterPos)
    }

    override fun toClientTag(p0: CompoundTag): CompoundTag = toTag(p0)

    override fun fromClientTag(p0: CompoundTag) = fromTag(null,p0)

    override fun tick() {
        // Value is always incremented to avoid desyncs
        if (world?.isClient == true) {
            ghostIngredientCycleIndex++
            if (ghostIngredientActive) {
                if (ghostIngredientCycleIndex % TicksPerSecond == 0) getMinecraftClient().scheduleRenderUpdate(pos)
            }
        }


    }

    /**
     * Inserts only one of the itemStack
     *
     * @return How many of the stack was taken
     */

    fun insertItem(itemStack: ItemStack): Int {
        val multiblock = multiblockIn ?: return 0
        val world = world!!
        markDirty()
        assert(isEmpty())
        lastChangeTime = world.time
        inventory.insert(itemStack.copy(count = 1))

        if (!itemStack.isEmpty) multiblock.filledHologramsCount++
        if (world.isServer) {
            if (multiblock.recipeHelpActive) {
                multiblock.bumpRecipeHelpCurrentLayerIfNeeded(world)
            }
        }

        return 1
    }

    fun getItem(): ItemStack = inventory.getInvStack(0)

    /**
     * May return an empty stack
     * @return A stack from the hologram everything went fine, null if the state is invalid
     */
    fun extractItem(): ItemStack? {
        val multiblock = multiblockIn ?: return null
        if (!this.isEmpty()) multiblock.filledHologramsCount--
        val item = inventory.extract(1)
        markDirty()
        return item
    }

    fun isEmpty() = getItem().isEmpty

    fun registerInventory(to: AttributeList<*>) = to.offer(inventory)

    // Note: we don't change recipe help here because this is only called when the entire multiblock is destroyed
    fun dropInventory() = world!!.dropItemStack(getItem(), pos)


    fun getMultiblockOrNull(): CrafterMultiblock? {
        val world = world!!
        // We just go down until we find a crafter
        var currentPos = pos.down()
        while (true) {
            val entityBelow = world.getBlockEntity(currentPos)
            if (entityBelow !is HologramBlockEntity) {
                return if (entityBelow is CrafterPieceEntity) entityBelow.multiblockIn
                else null
            }
            currentPos = currentPos.down()
        }
    }
//=======
////    fun getMultiblock() = getMultiblockOrNull()
////            ?: error("A hologram should always have a multiblock," +
////                    " and yet when looking at a crafter piece below at position $pos he did not have a multiblock instance.")
////
////    fun getMultiblockOrNull(): CrafterMultiblock? {
////        val world = world!!
////        // We just go down until we find a crafter
////        var currentPos = pos.down()
////        while (true) {
////            val entityBelow = world.getBlockEntity(currentPos)
////            if (entityBelow !is HologramBlockEntity) {
////                return if (entityBelow is CrafterPieceEntity) entityBelow.multiblockIn
////                else null
////            }
////            currentPos = currentPos.down()
////        }
////    }
//>>>>>>> ee6e919... commit of fix

    fun complainAboutMissingMultiblock() {
        println("********************** IF YOU SEE THIS, THIS IS A BUG, REPORT IT **********************\n" +
                "A hologram should always have a multiblock," +
                " and yet when looking at a crafter piece below at position $pos he did not have a multiblock instance.\n" +
                "*******************************************************************************************")
    }

}

