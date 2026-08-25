/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.CompilerTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "compiler")
public final class CompilerTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Only instructions count towards program length.")
    public static void onlyInstructionsCountTowardsProgramLength(final GameTestHelper helper) {
        CompilerTests.onlyInstructionsCountTowardsProgramLength(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Too many instructions is rejected.")
    public static void tooManyInstructionsIsRejected(final GameTestHelper helper) {
        CompilerTests.tooManyInstructionsIsRejected(helper);
    }

    // --------------------------------------------------------------------- //

    private CompilerTestsNeoForge() {
    }
}
