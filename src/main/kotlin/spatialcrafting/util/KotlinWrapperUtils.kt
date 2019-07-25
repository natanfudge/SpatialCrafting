package spatialcrafting.util

import com.sun.org.apache.xpath.internal.operations.Bool
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.stream.Stream


fun World.getBlock(location: BlockPos): Block = getBlockState(location).block
val World.isServer get() = !isClient

/**
 * Replaces the block in the [pos] with the specified [block], using the default [BlockState].
 */
fun World.setBlock(block: Block, pos: BlockPos) = world.setBlockState(pos, block.defaultState)


/**
 * Converts this into an [ItemStack] that holds exactly one of this.
 */
val ItemConvertible.itemStack get() = ItemStack(this)

private const val xKey = "x"
private const val yKey = "y"
private const val zKey = "z"
/**
 * Puts the [BlockPos]'s information with the specified [key].
 * Retrieve the [BlockPos] from a [CompoundTag] by using [getBlockPos] with the same [key].
 */
fun CompoundTag.putBlockPos(pos: BlockPos?, key: String) {
    if (pos == null) return
    putCompoundTag(key) {
        putInt(xKey, pos.x)
        putInt(yKey, pos.y)
        putInt(zKey, pos.z)
    }
}

/**
 * Retrieves the BlockPos from a [CompoundTag] with the specified [key], provided it was put there with [putBlockPos].
 *
 * Returns null if there is no [BlockPos] with the specified [key] in the [CompoundTag] (or if null was inserted).
 */
fun CompoundTag.getBlockPos(key: String): BlockPos? = transformCompoundTag(key) {
    BlockPos(
            getInt(xKey),
            getInt(yKey),
            getInt(zKey)
    )
}

/**
 * Constructs a [CompoundTag] on the spot and puts it with the specified [key].
 * @param init Add here the contents of the [CompoundTag].
 * @return this
 *
 * Example usage: see [putBlockPos]
 */
fun CompoundTag.putCompoundTag(key: String, init: CompoundTag.() -> Unit): CompoundTag {
    put(key, CompoundTag().apply(init))
    return this
}

/**
 * Retrieves the [CompoundTag] with the specified [key], and then builds an object using that tag.
 *
 * @param builder Build your desired object from the compound tag here.
 *
 * Returns null if there is no CompoundTag with the specified [key] in the [CompoundTag]  (or if null was inserted).
 *
 * Example usage: see [getBlockPos]
 */
fun <T> CompoundTag.transformCompoundTag(key: String, builder: CompoundTag.() -> T): T? {
    val tag = getTag(key) as? CompoundTag ?: return null
    return tag.run(builder)
}

//fun <T>CompoundTag.transformCompoundTag(key :String, builder : CompoundTag.() -> T) : T =


object Builders {
    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(vararg blocks: Block, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks).build(null)

    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(blocks: List<Block>, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks.toTypedArray()).build(null)

    /**
     * Creates a new [Material]
     */
    fun material(materialColor: MaterialColor, isLiquid: Boolean = false, isSolid: Boolean = true,
                 blocksMovement: Boolean = true, blocksLight: Boolean = true, requiresTool: Boolean = false,
                 burnable: Boolean = false, replaceable: Boolean = false,
                 pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Material = Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )


    /**
     * Creates a new [Block.Settings] with an existing material
     */
    fun blockSettings(material: Material, collidable: Boolean = true, slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f, resistance: Float = 0.0f, dropsLike: Block? = null): Block.Settings =
            Block.Settings.of(material).apply {
                if (!collidable) noCollision()
                slipperiness(slipperiness)
                strength(hardness, resistance)
                if (dropsLike != null) dropsLike(dropsLike)
            }

    /**
     * Creates a new [Block.Settings] by building its material on the spot.
     */
    fun blockSettings(materialColor: MaterialColor,
                      collidable: Boolean = true,
                      slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f,
                      resistance: Float = 0.0f,
                      dropsLike: Block? = null,
                      isLiquid: Boolean = false,
                      isSolid: Boolean = true,
                      blocksMovement: Boolean = true,
                      blocksLight: Boolean = true,
                      requiresTool: Boolean = false,
                      burnable: Boolean = false,
                      replaceable: Boolean = false,
                      pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Block.Settings = Block.Settings.of(Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )
    ).apply {
        if (!collidable) noCollision()
        slipperiness(slipperiness)
        strength(hardness, resistance)
        if (dropsLike != null) dropsLike(dropsLike)
    }


//    ) = Block.Settings.of(
//            Material(
//                    materialColor,
//                    isLiquid,
//                    isSolid,
//                    blocksMovement,
//                    blocksLight,
//                    !requiresTool,
//                    burnable,
//                    replaceable,
//                    pistonBehavior
//            )
//    )


}
//MaterialColor.BLUE, // color
//        false, // liquid
//        false, //solid
//        false, //blocksMovement
//        false, //blockLight
//        false, // can be broken by hand
//        false, //burnable
//        false, //replaceable
//        PistonBehavior.IGNORE //pistonBehavior


/**
 * Sends a packet from the server to the client for all the players in the stream.
 * @param packetBuilder Put the information you wish to send here
 */
fun Stream<ServerPlayerEntity>.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    for (player in this) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packetId, packet)
    }
}



