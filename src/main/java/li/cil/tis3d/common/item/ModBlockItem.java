package li.cil.tis3d.common.item;

import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ModBlockItem extends BlockItem {
    public ModBlockItem(final Block block, final Properties properties) {
        super(block, properties.group(ItemGroups.COMMON));
    }

    public ModBlockItem(final Block block) {
        this(block, createProperties());
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }

    // --------------------------------------------------------------------- //

    protected static Properties createProperties() {
        return new Properties();
    }
}
