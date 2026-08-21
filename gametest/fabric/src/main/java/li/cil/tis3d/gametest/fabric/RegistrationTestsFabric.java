/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.fabric;

import li.cil.tis3d.gametest.RegistrationTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static li.cil.tis3d.gametest.fabric.FabricTestSupport.TEMPLATE;

public final class RegistrationTestsFabric {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public void thirdPartyModuleProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyModuleProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public void thirdPartyRedstoneInputProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyRedstoneInputProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public void thirdPartySerialInterfaceProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartySerialInterfaceProviderIsUsed(helper);
    }
}
