package li.cil.tis3d.common.item.fabric;

import net.fabricmc.loader.api.FabricLoader;

public final class FacadeModuleItemImpl {
    public static boolean isEnabled() {
        return !FabricLoader.getInstance().isModLoaded("sodium");
    }
}
