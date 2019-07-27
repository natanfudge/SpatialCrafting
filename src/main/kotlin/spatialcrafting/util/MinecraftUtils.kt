package spatialcrafting.util

import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.reflect.KProperty


val BlockPos.xz get() = "($x,$z)"

fun LivingEntity?.sendMessage(message: String) {
//    println(message)
    if (this == null) return
    this.sendMessage(LiteralText(message))
}

fun World.name() = if(isClient) "Client" else "Server"


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