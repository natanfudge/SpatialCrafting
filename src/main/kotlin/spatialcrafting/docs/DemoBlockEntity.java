package spatialcrafting.docs;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import spatialcrafting.MyMod;

public class DemoBlockEntity extends BlockEntity {
    public DemoBlockEntity() {
        super(MyMod.INSTANCE.getMyBlockEntityType());
    }


}
