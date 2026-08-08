package li.cil.tis3d.common.item;

import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ModBlockItem extends BlockItem {
    public ModBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    public ModBlockItem(final Block block) {
        this(block, createProperties());
    }

    // --------------------------------------------------------------------- //

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }

    // --------------------------------------------------------------------- //

    protected static Properties createProperties() {
        return new Properties();
    }
}
