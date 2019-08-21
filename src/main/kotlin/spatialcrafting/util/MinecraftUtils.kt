@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IWorld
import net.minecraft.world.World
import kotlin.math.roundToInt
import kotlin.math.sqrt


val BlockPos.xz get() = "($x,$z)"

fun World.play(soundEvent: SoundEvent,at: BlockPos,
                    ofCategory: SoundCategory,toPlayer: PlayerEntity? = null,  volumeMultiplier: Float = 1.0f, pitchMultiplier: Float = 1.0f) : Unit
        = playSound(toPlayer, at,soundEvent, ofCategory, volumeMultiplier, pitchMultiplier)

fun LivingEntity?.sendMessage(message: String) {
//    println(message)
    if (this == null) return
    this.sendMessage(LiteralText(message))
}

fun Vec3d.toBlockPos() = BlockPos(x.roundToInt(), y.roundToInt(), z.roundToInt())
inline fun Ingredient.matches(itemStack: ItemStack) = method_8093(itemStack)


fun DrawableHelper.drawCenteredStringWithoutShadow(textRenderer_1: TextRenderer, string_1: String?, int_1: Int, int_2: Int, int_3: Int) {
    textRenderer_1.draw(string_1, (int_1 - textRenderer_1.getStringWidth(string_1) / 2).toFloat(), int_2.toFloat(), int_3)
}

fun getMinecraftClient() : MinecraftClient = MinecraftClient.getInstance()

fun BlockPos.distanceFrom(otherPos :Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())

val  PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand
open class DefaultBlock : Block(Settings.of(Material.STONE))

fun PlayerEntity.isHoldingItemIn(hand: Hand): Boolean = !getStackInHand(hand).isEmpty

val PacketContext.world: World get() = player.world

/**
 * Creates a new [ItemStack] with the specified [count].
 */
fun ItemStack.copy(count: Int) = copy().apply { this.count = count }


/**
 * Converts this into an [ItemStack] that holds exactly one of this.
 */
val ItemConvertible.itemStack get() = ItemStack(this)

fun World.getBlock(location: BlockPos): Block = getBlockState(location).block
val World.isServer get() = !isClient
/**
 * Replaces the block in the [pos] with the specified [block], using the default [BlockState].
 */
fun IWorld.setBlock(block: Block, pos: BlockPos): Boolean = world.setBlockState(pos, block.defaultState)

fun World.name() = if (isClient) "Client" else "Server"
fun World.dropItemStack(stack: ItemStack, pos: BlockPos) : ItemEntity {
    return ItemEntity(world, pos.x.d, pos.y.d, pos.z.d, stack).also { world.spawnEntity(it) }
}