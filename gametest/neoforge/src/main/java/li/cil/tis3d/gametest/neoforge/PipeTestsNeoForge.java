package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.PipeTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class PipeTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesBetweenFacesOfOneCasing(final GameTestHelper helper) {
        PipeTests.valueCrossesBetweenFacesOfOneCasing(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesCasingBoundary(final GameTestHelper helper) {
        PipeTests.valueCrossesCasingBoundary(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void writeCompleteFiresAfterTransfer(final GameTestHelper helper) {
        PipeTests.writeCompleteFiresAfterTransfer(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void cancelledWriteDoesNotTransfer(final GameTestHelper helper) {
        PipeTests.cancelledWriteDoesNotTransfer(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void readerWithoutWriterBlocks(final GameTestHelper helper) {
        PipeTests.readerWithoutWriterBlocks(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void pipeStateSurvivesReload(final GameTestHelper helper) {
        PipeTests.pipeStateSurvivesReload(helper);
    }

    // --------------------------------------------------------------------- //

    private PipeTestsNeoForge() {
    }
}
