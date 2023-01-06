package li.cil.tis3d.mixin.forge;

import li.cil.tis3d.common.item.KeyItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(KeyItem.class)
public abstract class MixinKeyItem implements IForgeItem {
    @Nullable
    @Override
    public CompoundTag getShareTag(final ItemStack stack) {
        return null;
    }
}
