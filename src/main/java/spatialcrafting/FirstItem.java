package spatialcrafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class FirstItem extends Item {

    public FirstItem() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(ItemGroup.FOOD));
        setRegistryName("firstitem");
    }
}