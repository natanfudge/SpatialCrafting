package spatialcrafting

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.Container
import net.minecraft.container.ContainerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import spatialcrafting.util.Builders

val  HologramBlockEntityType =  Builders.blockEntityType(HologramBlock) { HologramBlockEntity() }
class HologramBlockEntity : BlockEntity(HologramBlockEntityType) {

    fun dropInventory(){
        //TODO
    }

    //TODO: store 1 inventory slot and save/load to disk
}