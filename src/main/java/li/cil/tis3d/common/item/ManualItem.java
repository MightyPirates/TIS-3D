package li.cil.tis3d.common.item;

import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The manual!
 */
public final class ManualItem extends li.cil.manual.api.prefab.ManualItem {
    public ManualItem() {
        super(new Properties().tab(ItemGroups.COMMON), Manuals.MANUAL);
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }
}
