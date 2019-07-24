package spatialcrafting;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

class test extends Item {

    public test(Settings item$Settings_1) {
        super(item$Settings_1);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        int currentDay = 4;
        int currentMonth = 7;
        tooltip.add(new TranslatableText("item.tutorial.fabric_item.tooltip", currentDay, currentMonth));
    }
}

