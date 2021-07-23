package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ItemGroups {
    public static final CreativeModeTab COMMON = new CreativeModeTab(API.MOD_ID + ".common") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.CONTROLLER.get());
        }
    };
}
