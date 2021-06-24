package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class ItemGroups {
    public static final ItemGroup COMMON = new ItemGroup(API.MOD_ID + ".common") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.CONTROLLER.get());
        }
    };
}
