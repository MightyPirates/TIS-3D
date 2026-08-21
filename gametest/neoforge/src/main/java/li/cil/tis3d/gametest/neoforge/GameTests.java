/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.GameTestReporting;
import li.cil.tis3d.gametest.TestRegistry;
import net.neoforged.fml.common.Mod;

import java.io.File;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;

@Mod(MOD_ID)
public final class GameTests {
    private static final String JUNIT_OUTPUT_DIR_PROPERTY = "tis3d.gameTest.junitDir";
    private static final String REPORT_FILE_NAME = "neoforge-game-tests.xml";

    public GameTests() {
        TestRegistry.initialize();

        final String directory = System.getProperty(JUNIT_OUTPUT_DIR_PROPERTY);
        GameTestReporting.install(directory == null || directory.isEmpty()
            ? null
            : new File(directory, REPORT_FILE_NAME));
    }
}
