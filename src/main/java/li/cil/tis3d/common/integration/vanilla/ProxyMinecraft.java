package li.cil.tis3d.common.integration.vanilla;

import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ProxyMinecraft implements ModProxy {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(new RedstoneInputProviderVanilla());
    }
}
