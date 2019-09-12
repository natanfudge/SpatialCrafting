import kotlinx.serialization.KSerializer

//package spatialcrafting.util
//
//import kotlinx.serialization.KSerializer
//
interface ISerializable<T : ISerializable<T>>{
    val serializer : KSerializer<T>
}