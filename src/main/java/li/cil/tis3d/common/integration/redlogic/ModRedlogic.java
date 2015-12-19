package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.RegistryBundledRedstone;

public final class ModRedLogic implements ModProxy {
    public static final String MOD_ID = "RedLogic";

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        RegistryBundledRedstone.INSTANCE.addCallback(RedLogicCallback::onBundledOutputChanged);
    }
}
