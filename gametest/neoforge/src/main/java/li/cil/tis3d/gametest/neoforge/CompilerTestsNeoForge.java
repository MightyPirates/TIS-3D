/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.CompilerTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompilerTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void onlyInstructionsCountTowardsProgramLength(final GameTestHelper helper) {
        CompilerTests.onlyInstructionsCountTowardsProgramLength(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void tooManyInstructionsIsRejected(final GameTestHelper helper) {
        CompilerTests.tooManyInstructionsIsRejected(helper);
    }

    // --------------------------------------------------------------------- //

    private CompilerTestsNeoForge() {
    }
}
