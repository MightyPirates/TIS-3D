package li.cil.tis3d.gametest.fabric;

import li.cil.tis3d.api.platform.FabricProviderInitializer;
import li.cil.tis3d.gametest.GameTestReporting;
import li.cil.tis3d.gametest.TestRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.File;

public final class GameTestsFabric implements FabricProviderInitializer {
    private static final String REPORT_FILE_PROPERTY = "fabric-api.gametest.report-file";

    @Override
    public void registerProviders() {
        TestRegistry.initialize();

        // Make sure our reporter wins.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            final String report = System.getProperty(REPORT_FILE_PROPERTY);
            GameTestReporting.install(report == null || report.isEmpty() ? null : new File(report));
        });
    }
}
