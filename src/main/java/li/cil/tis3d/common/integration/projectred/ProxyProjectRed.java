package li.cil.tis3d.common.integration.projectred;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;

public class ProxyProjectRed implements ModProxy {
    public static final String MOD_ID = "ProjRed|Transmission";

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        ProjectRedTileInteraction.INSTANCE.register();
        RedstoneIntegration.INSTANCE.addBundledRedstoneInputProvider(ProjectRedCallbacks::getBundledInput);
    }
}
