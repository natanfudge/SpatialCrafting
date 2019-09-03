@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

import com.mojang.blaze3d.platform.GlStateManager
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IWorld
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import spatialcrafting.client.particle.plus
import spatialcrafting.client.particle.toVec3d
import kotlin.math.roundToInt
import kotlin.math.sqrt


val BlockPos.xz get() = "($x,$z)"

fun BlockPos.distanceFrom(otherPos: Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())

operator fun BlockPos.plus(vec3d: Vec3d) = this.toVec3d() + vec3d

fun CompoundTag.putBlockPos(key: String, pos: BlockPos?) = if (pos != null) putLong(key, pos.asLong()) else Unit

fun CompoundTag.getBlockPos(key: String): BlockPos? = getLong(key).let { if (it == 0.toLong()) null else BlockPos.fromLong(it) }

fun Vec3d.toBlockPos() = BlockPos(x.roundToInt(), y.roundToInt(), z.roundToInt())
fun vec3d(x: Double, y: Double, z: Double) = Vec3d(x, y, z)

fun IWorld.play(soundEvent: SoundEvent, at: BlockPos,
               ofCategory: SoundCategory, toPlayer: PlayerEntity? = null, volumeMultiplier: Float = 1.0f, pitchMultiplier: Float = 1.0f): Unit = playSound(toPlayer, at, soundEvent, ofCategory, volumeMultiplier, pitchMultiplier)

fun IWorld.getBlock(location: BlockPos): Block = getBlockState(location).block

fun IWorld.setBlock(block: Block, pos: BlockPos, blockState: BlockState = block.defaultState): Boolean
        = world.setBlockState(pos, blockState)

val IWorld.isServer get() = !isClient

val IWorld.name get() = if (isClient) "Client" else "Server"

fun IWorld.dropItemStack(stack: ItemStack, pos: BlockPos): ItemEntity = dropItemStack(stack, pos.toVec3d())

fun IWorld.dropItemStack(stack: ItemStack, pos: Vec3d): ItemEntity {
    return ItemEntity(world, pos.x, pos.y, pos.z, stack).also { world.spawnEntity(it) }
}



fun ItemStack.copy(count: Int): ItemStack = copy().apply { this.count = count }

val ItemConvertible.itemStack get() = ItemStack(this)

inline fun Ingredient.matches(itemStack: ItemStack) = method_8093(itemStack)


/**
 * Note that what is held in the main hand still exists in the inventory, so it includes that.
 */
val PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand


fun PlayerEntity.isHoldingItemIn(hand: Hand): Boolean = !getStackInHand(hand).isEmpty

fun PlayerEntity.offerOrDrop(itemStack: ItemStack) = inventory.offerOrDrop(world, itemStack)


class ToolMaterialImpl(private val _miningLevel: Int,
                       private val _durability: Int,
                       private val _miningSpeed: Float,
                       private val _attackDamage: Float,
                       private val _enchantability: Int,
                       private val _repairIngredient: () -> Ingredient) : ToolMaterial {
    override fun getRepairIngredient(): Ingredient = _repairIngredient()
    override fun getDurability(): Int = _durability
    override fun getEnchantability(): Int = _enchantability
    override fun getMiningSpeed(): Float = _miningSpeed
    override fun getMiningLevel(): Int = _miningLevel
    override fun getAttackDamage(): Float = _attackDamage

}

