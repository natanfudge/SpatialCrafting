package spatialcrafting.util.kotlinwrappers

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos


/**
 * Puts the [BlockPos]'s information with the specified [key].
 * Retrieve the [BlockPos] from a [CompoundTag] by using [getBlockPos] with the same [key].
 */
fun CompoundTag.putBlockPos(key: String, pos: BlockPos?) {
    if (pos == null) return
    putLong(key, pos.asLong())
}

/**
 * Retrieves the BlockPos from a [CompoundTag] with the specified [key], provided it was put there with [putBlockPos].
 *
 * Returns null if there is no [BlockPos] with the specified [key] in the [CompoundTag] (or if null was inserted).
 */
fun CompoundTag.getBlockPos(key: String): BlockPos? = BlockPos.fromLong(getLong(key))

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


//
//
//interface SerializableBase {
//    fun addToTag(tag: CompoundTag, key: String)
//}
//
//abstract class Serializable(private vararg val serials: SerializableBase) : SerializableBase {
//    fun toTag(): CompoundTag = CompoundTag().apply {
//        serials.forEachIndexed { index, serializable ->
//            serializable.addToTag(this, index.toString())
//        }
//    }
//
//    override fun addToTag(tag: CompoundTag, key: String) {
//        tag.put(key, toTag())
//    }
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
//fun <T> CompoundTag.deserialize(deserializer: DeserializationContext.() -> T): T =
//        DeserializationContext(this).deserializer()
//
//class DeserializationContext(private val tag: CompoundTag) {
//    private var count: Int = 0
//
//    private fun nextString() = count++.toString()
//
//    val string get() : String = tag.getString(nextString())
//    val blockPos get() : BlockPos = tag.getBlockPos(nextString())!!
//    val int get(): Int = tag.getInt(nextString())
//
//    fun <T> custom(deserializer: Deserializer<T>): T = deserializer.fromTag(tag.getTag(nextString()) as CompoundTag)
//
//}
//
//
//interface Deserializer<T> {
//    fun fromTag(tag: CompoundTag): T
//}
//
//data class MyClass(val name: String, val pos: BlockPos, val age: Int) : Serializable(name.sr, pos.sr, age.sr) {
//    companion object : Deserializer<MyClass> {
//        override fun fromTag(tag: CompoundTag) = tag.deserialize {
//            MyClass(string, blockPos, int)
//        }
//
//    }
//}
//
//data class NestedClass(val clazz: MyClass) : Serializable(clazz) {
//    companion object {
//        fun fromTag(tag: CompoundTag) = tag.deserialize {
//            NestedClass(custom(MyClass))
//        }
//    }
//}
//
//
//fun main() {
//    val x = MyClass(name = "amar", pos = BlockPos(1, 2, 3), age = 12)
//    val nested = NestedClass(x)
//    val tag = nested.toTag()
////    println("Tag = $tag")
//    val back = NestedClass.fromTag(tag)
//    println(back)
//}
//
//
//
//
//
//
//
