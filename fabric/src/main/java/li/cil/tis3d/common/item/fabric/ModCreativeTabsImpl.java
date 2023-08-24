package li.cil.tis3d.common.item.fabric;

import li.cil.tis3d.common.item.Items;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;

public final class ModCreativeTabsImpl {
    public static boolean isItemEnabled(final Item item) {
        if (FabricLoader.getInstance().isModLoaded("sodium") && item == Items.FACADE_MODULE.get()) {
            return false;
        }
        return true;
    }
}
