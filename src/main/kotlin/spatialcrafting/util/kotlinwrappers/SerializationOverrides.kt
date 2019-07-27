//package spatialcrafting.util.kotlinwrappers
//
//import net.minecraft.nbt.CompoundTag
//import net.minecraft.util.math.BlockPos
//
//interface Factory<T> {
//    fun fromTag(tag: CompoundTag): T
//}
//
//interface SerializableBase {
//    fun addToTag(tag: CompoundTag, key: String)
//}
////
////abstract class Serializable(private vararg val serials: SerializableBase) : SerializableBase {
////    fun toTag(): CompoundTag = CompoundTag().apply {
////        serials.forEachIndexed { index, serializable ->
////            serializable.addToTag(this, index.toString())
////        }
////    }
////
////    override fun addToTag(tag: CompoundTag, key: String) {
////        tag.put(key, toTag())
////    }
////}
//
//interface Serializable3 : SerializableBase {
//    val value1: SerializableBase
//    val value2: SerializableBase
//    val value3: SerializableBase
//
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.put(key, toTag())
//    }
//
//    fun toTag(): CompoundTag = CompoundTag().apply {
//        value1.addToTag(this, "value1")
//        value2.addToTag(this, "value2")
//        value3.addToTag(this, "value3")
//    }
//}
//
//fun fromSerial3(compoundTag: CompoundTag): MyClass {
//    MyClass()
//}
//
//
//inline class SerializableInt(private val int: Int) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putInt(key, int)
//    }
//}
//
//val Int.sr get() = SerializableInt(this)
//
//inline class SerializableString(private val string: String) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putString(key, string)
//    }
//}
//
//val String.sr get() = SerializableString(this)
//
//inline class SerializableBlockPos(private val blockPos: BlockPos) : SerializableBase {
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.putBlockPos(key, blockPos)
//    }
//}
//
//val BlockPos.sr get() = SerializableBlockPos(this)
//
//data class MyClass(val name: String, val pos: BlockPos, val age: Int) : Serializable(name.sr, pos.sr, age.sr) {
//    companion object {
//        fun fromTag(tag: CompoundTag): MyClass {
//            return MyClass(tag.getString("0"), tag.getBlockPos("1")!!, tag.getInt("2"))
//        }
//
//    }
//}
//
////fun CompoundTag.
//
//
//fun main() {
//    val x = MyClass(name = "amar", pos = BlockPos(1, 2, 3), age = 12)
//    val tag = x.toTag()
//    print(tag)
//}
//
