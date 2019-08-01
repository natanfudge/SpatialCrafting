@file:Suppress("NOTHING_TO_INLINE")

package spatialcrafting.util

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