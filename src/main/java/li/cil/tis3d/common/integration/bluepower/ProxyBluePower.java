package li.cil.tis3d.common.integration.bluepower;

import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;

public class ProxyBluePower implements ModProxy {
    public static final String MOD_ID = "bluepowerAPI";

    @Override
    public boolean isAvailable() {
        return ModAPIManager.INSTANCE.hasAPI(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        BluePowerProvider.INSTANCE.register();
        RedstoneIntegration.INSTANCE.addCallback(BluePowerCallbacks::onBundledOutputChanged);
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(BluePowerCallbacks::getInput);
        RedstoneIntegration.INSTANCE.addBundledRedstoneInputProvider(BluePowerCallbacks::getBundledInput);
    }
}
