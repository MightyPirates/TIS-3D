/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.MachineTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class MachineTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void controllerScansAndRuns(final GameTestHelper helper) {
        MachineTests.controllerScansAndRuns(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void controllerRejectsTwoControllers(final GameTestHelper helper) {
        MachineTests.controllerRejectsTwoControllers(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void controllerRejectsTooManyCasings(final GameTestHelper helper) {
        MachineTests.controllerRejectsTooManyCasings(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void disablingControllerDisablesModules(final GameTestHelper helper) {
        MachineTests.disablingControllerDisablesModules(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void stepsOncePerTickAtFullPower(final GameTestHelper helper) {
        MachineTests.stepsOncePerTickAtFullPower(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void stepsEveryOtherTickAtPartialPower(final GameTestHelper helper) {
        MachineTests.stepsEveryOtherTickAtPartialPower(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void casingLocksAndUnlocksWithKey(final GameTestHelper helper) {
        MachineTests.casingLocksAndUnlocksWithKey(helper);
    }

    // --------------------------------------------------------------------- //

    private MachineTestsNeoForge() {
    }
}
