@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
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

fun BlockPos.distanceFrom(otherPos :Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())

val  PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand
open class DefaultBlock : Block(Settings.of(Material.STONE))

//class IdentifiedValue<T>(var value: T, val identifier: String)
//
//infix fun <T> T?.identifiedByNullable(identifier: String): IdentifiedValue<T?> = IdentifiedValue(this, identifier)
//infix fun <T> T.identifiedBy(identifier: String): IdentifiedValue<T> = IdentifiedValue(this, identifier)
//
//fun CompoundTag.putBlockPos(pos: IdentifiedValue<BlockPos>) = putBlockPos(pos.value, pos.identifier)
//fun CompoundTag.getBlockPos(identified: IdentifiedValue<BlockPos>): BlockPos? = getBlockPos(identified.identifier)
//class Identified {
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): IdentifiedValue {
//        return "$thisRef, thank you for delegating '${property.name}' to me!"
//    }
//
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//        println("$value has been assigned to '${property.name}' in $thisRef.")
//    }
//}