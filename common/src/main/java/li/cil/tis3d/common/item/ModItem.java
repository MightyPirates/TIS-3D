package li.cil.tis3d.common.item;

import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ModItem extends Item {
    public ModItem(final Properties properties) {
        super(properties);
    }

    public ModItem() {
        this(createProperties());
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    // --------------------------------------------------------------------- //

    protected static Properties createProperties() {
        return new Properties();
    }
}
