package li.cil.tis3d.client.mixin;

import li.cil.tis3d.client.inject.ItemUsageContextAccessors;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemUsageContext.class)
public abstract class ItemUsageContextMixin implements ItemUsageContextAccessors {
    @Shadow
    @Final
    protected BlockHitResult field_17543;

    @Override
    public BlockHitResult getBlockHitResult() {
        return field_17543;
    }
}
