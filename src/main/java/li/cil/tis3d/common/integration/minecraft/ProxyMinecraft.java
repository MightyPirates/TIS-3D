package li.cil.tis3d.common.integration.minecraft;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;

public class ProxyMinecraft implements ModProxy {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(RedstoneInputProviderMinecraft::getInput);
    }
}
