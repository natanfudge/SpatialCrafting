package spatialcrafting

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag

import java.util.function.Supplier

val DEMO_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create(Supplier { DemoBlockEntity() }, EXAMPLE_BLOCK).build(null)

class DemoBlockEntity : BlockEntity(DEMO_BLOCK_ENTITY_TYPE) {

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