/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.RegistrationTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "registration")
public final class RegistrationTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Third party module provider is used.")
    public static void thirdPartyModuleProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyModuleProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Third party redstone input provider is used.")
    public static void thirdPartyRedstoneInputProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyRedstoneInputProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Third party serial interface provider is used.")
    public static void thirdPartySerialInterfaceProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartySerialInterfaceProviderIsUsed(helper);
    }

    // --------------------------------------------------------------------- //

    private RegistrationTestsNeoForge() {
    }
}
