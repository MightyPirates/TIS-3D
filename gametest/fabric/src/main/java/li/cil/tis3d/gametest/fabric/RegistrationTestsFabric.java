/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.fabric;

import li.cil.tis3d.gametest.RegistrationTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class RegistrationTestsFabric {
    @GameTest(maxTicks = 200)
    public void thirdPartyModuleProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyModuleProviderIsUsed(helper);
    }

    @GameTest(maxTicks = 200)
    public void thirdPartyRedstoneInputProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyRedstoneInputProviderIsUsed(helper);
    }

    @GameTest(maxTicks = 200)
    public void thirdPartySerialInterfaceProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartySerialInterfaceProviderIsUsed(helper);
    }
}
