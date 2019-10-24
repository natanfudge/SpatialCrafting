package spatialcrafting.util

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.client.sound.*
import net.minecraft.item.Item
import net.minecraft.item.SwordItem
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d
import spatialcrafting.SpatialCraftingItemGroup


object Builders {
    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(vararg blocks: Block, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks).build(null)

    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(blocks: List<Block>, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks.toTypedArray()).build(null)

    /**
     * Creates a new [Material]
     */
    fun material(materialColor: MaterialColor, isLiquid: Boolean = false, isSolid: Boolean = true,
                 blocksMovement: Boolean = true, blocksLight: Boolean = true, requiresTool: Boolean = false,
                 burnable: Boolean = false, replaceable: Boolean = false,
                 pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Material = Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )


    /**
     * Creates a new [Block.Settings] with an existing material
     */
    fun blockSettings(material: Material, collidable: Boolean = true, slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f, resistance: Float = 0.0f, dropsLike: Block? = null): Block.Settings =
            Block.Settings.of(material).apply {
                if (!collidable) noCollision()
                slipperiness(slipperiness)
                strength(hardness, resistance)
                if (dropsLike != null) dropsLike(dropsLike)
            }

    /**
     * Creates a new [Block.Settings] by building its material on the spot.
     */
    fun blockSettings(materialColor: MaterialColor,
                      collidable: Boolean = true,
                      slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f,
                      resistance: Float = 0.0f,
                      dropsLike: Block? = null,
                      isLiquid: Boolean = false,
                      isSolid: Boolean = true,
                      blocksMovement: Boolean = true,
                      blocksLight: Boolean = true,
                      requiresTool: Boolean = false,
                      burnable: Boolean = false,
                      replaceable: Boolean = false,
                      pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Block.Settings = Block.Settings.of(Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )
    ).apply {
        if (!collidable) noCollision()
        slipperiness(slipperiness)
        strength(hardness, resistance)
        if (dropsLike != null) dropsLike(dropsLike)
    }

    /**
     * Creates a new sword
     */
    fun sword(durability: Int, damage: Int, attackSpeed: Float, enchantability: Int, repairMaterial: () -> Ingredient) = SwordItem(ToolMaterialImpl(
            _miningLevel = 0,
            _durability = durability,
            _miningSpeed = 0f,
            _attackDamage = 0f,
            _enchantability = enchantability,
            _repairIngredient = repairMaterial
    ),
            damage - 1,
            attackSpeed - 4,
            Item.Settings().group(SpatialCraftingItemGroup)
    )


}

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




object ClientBuilders {

    /**
     * Creates a new SoundInstance (client only)
     */
    @Environment(EnvType.CLIENT)
    fun soundInstance(soundEvent: SoundEvent,
                      category: SoundCategory,
                      pos: Vec3d,
                      volume: Float = 1.0f,
                      pitch: Float = 1.0f,
                      repeats: Boolean = false,
                      repeatDelay: Int = 0,
                      attenuationType: SoundInstance.AttenuationType = SoundInstance.AttenuationType.LINEAR,
                      relative: Boolean = false): SoundInstance {
        return PositionedSoundInstance(soundEvent.id, category, volume, pitch, repeats, repeatDelay, attenuationType,
                pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), relative)

    }
}