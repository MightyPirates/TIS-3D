/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.PipeTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "pipe")
public final class PipeTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses between faces of one casing.")
    public static void valueCrossesBetweenFacesOfOneCasing(final GameTestHelper helper) {
        PipeTests.valueCrossesBetweenFacesOfOneCasing(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses casing boundary.")
    public static void valueCrossesCasingBoundary(final GameTestHelper helper) {
        PipeTests.valueCrossesCasingBoundary(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Write complete fires after transfer.")
    public static void writeCompleteFiresAfterTransfer(final GameTestHelper helper) {
        PipeTests.writeCompleteFiresAfterTransfer(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Cancelled write does not transfer.")
    public static void cancelledWriteDoesNotTransfer(final GameTestHelper helper) {
        PipeTests.cancelledWriteDoesNotTransfer(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Reader without writer blocks.")
    public static void readerWithoutWriterBlocks(final GameTestHelper helper) {
        PipeTests.readerWithoutWriterBlocks(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Pipe state survives reload.")
    public static void pipeStateSurvivesReload(final GameTestHelper helper) {
        PipeTests.pipeStateSurvivesReload(helper);
    }

    // --------------------------------------------------------------------- //

    private PipeTestsNeoForge() {
    }
}
