package li.cil.tis3d.common.integration.minecraft;

import li.cil.tis3d.api.SerialAPI;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public final class ProxyMinecraft implements ModProxy {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(RedstoneInputProviderMinecraft::getInput);

        SerialAPI.addProvider(new SerialInterfaceProviderFurnace());
    }
}
