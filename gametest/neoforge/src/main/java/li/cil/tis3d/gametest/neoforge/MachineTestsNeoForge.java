/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.MachineTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "machine")
public final class MachineTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Controller scans and runs.")
    public static void controllerScansAndRuns(final GameTestHelper helper) {
        MachineTests.controllerScansAndRuns(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Controller rejects two controllers.")
    public static void controllerRejectsTwoControllers(final GameTestHelper helper) {
        MachineTests.controllerRejectsTwoControllers(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Controller rejects too many casings.")
    public static void controllerRejectsTooManyCasings(final GameTestHelper helper) {
        MachineTests.controllerRejectsTooManyCasings(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Disabling controller disables modules.")
    public static void disablingControllerDisablesModules(final GameTestHelper helper) {
        MachineTests.disablingControllerDisablesModules(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Steps once per tick at full power.")
    public static void stepsOncePerTickAtFullPower(final GameTestHelper helper) {
        MachineTests.stepsOncePerTickAtFullPower(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Steps every other tick at partial power.")
    public static void stepsEveryOtherTickAtPartialPower(final GameTestHelper helper) {
        MachineTests.stepsEveryOtherTickAtPartialPower(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Casing locks and unlocks with key.")
    public static void casingLocksAndUnlocksWithKey(final GameTestHelper helper) {
        MachineTests.casingLocksAndUnlocksWithKey(helper);
    }

    // --------------------------------------------------------------------- //

    private MachineTestsNeoForge() {
    }
}
