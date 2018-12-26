package li.cil.tis3d.common.integration.minecraft;

import li.cil.tis3d.api.SerialAPI;
import li.cil.tis3d.common.integration.ModProxy;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;

public final class MinecraftProxy implements ModProxy {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init() {
        RedstoneIntegration.INSTANCE.addRedstoneInputProvider(MinecraftCallbacks::getInput);

        SerialAPI.addProvider(new FurnaceSerialInterfaceProvider());
    }
}
