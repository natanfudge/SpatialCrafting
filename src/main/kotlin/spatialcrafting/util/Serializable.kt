package spatialcrafting.util

import net.minecraft.nbt.CompoundTag

interface Serializable<T> {
    //    fun addToTag(tag: CompoundTag, key: String) : CompoundTag
    fun toTag(): CompoundTag

}

//fun <T : Serializable<T>> CompoundTag.putSerializable(serializable: Serializable<T>, key :String){
//
//}

//fun <T : Serializable<T>> CompoundTag.putIdentified(identified: IdentifiedValue<T>) = identified.value.addToTag(this, identified.identifier)
//fun <T : Serializable<T>?> CompoundTag.getIdentified(identified: IdentifiedValue<T>): T? = identified.value?.fromTag(this, identified.identifier)

//fun <T> Serializable<T>?.addToTag(tag: CompoundTag, key : String) {
//    this?.addToTag(tag, key)
//}