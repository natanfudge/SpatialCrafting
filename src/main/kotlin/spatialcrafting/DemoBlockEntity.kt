package spatialcrafting

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag

import java.util.function.Supplier

val DemoBlockEntityType = BlockEntityType.Builder.create(Supplier { DemoBlockEntity() }, CrafterPieceX2).build(null)

class DemoBlockEntity : BlockEntity(DemoBlockEntityType) {

    // Store the current value of the number
    private var number = 7

    // Serialize the BlockEntity
    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)

        // Save the current value of the number to the tag
        tag.putInt("number", number)

        return tag
    }

    // Deserialize the BlockEntity
    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        number = tag.getInt("number")
        println("Loading Number = $number!")
    }
}