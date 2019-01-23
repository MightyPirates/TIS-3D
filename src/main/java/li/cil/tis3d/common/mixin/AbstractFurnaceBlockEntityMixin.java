package li.cil.tis3d.common.mixin;

import li.cil.tis3d.common.inject.AbstractFurnaceBlockEntityAccessors;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin implements AbstractFurnaceBlockEntityAccessors {
    @Shadow
    private int burnTime;

    @Shadow
    private int fuelTime;

    @Shadow
    private int cookTime;

    @Shadow
    private int cookTimeTotal;

    @Override
    public int getBurnTime() {
        return burnTime;
    }

    @Override
    public int getFuelTime() {
        return fuelTime;
    }

    @Override
    public int getCookTime() {
        return cookTime;
    }

    @Override
    public int getCookTimeTotal() {
        return cookTimeTotal;
    }
}
