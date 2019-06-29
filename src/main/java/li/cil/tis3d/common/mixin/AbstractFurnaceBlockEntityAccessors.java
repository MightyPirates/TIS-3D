package li.cil.tis3d.common.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessors {
    @Accessor
    int getBurnTime();

    @Accessor
    int getFuelTime();

    @Accessor
    int getCookTime();

    @Accessor
    int getCookTimeTotal();
}
