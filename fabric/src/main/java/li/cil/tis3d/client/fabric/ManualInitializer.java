package li.cil.tis3d.client.fabric;

import li.cil.manual.api.platform.FabricManualInitializer;
import li.cil.tis3d.client.manual.Manuals;

public final class ManualInitializer implements FabricManualInitializer {
    @Override
    public void registerManualObjects() {
        Manuals.initialize();
    }
}
