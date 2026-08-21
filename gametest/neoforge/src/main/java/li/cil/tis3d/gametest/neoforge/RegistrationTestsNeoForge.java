package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.RegistrationTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class RegistrationTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void thirdPartyModuleProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyModuleProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void thirdPartyRedstoneInputProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartyRedstoneInputProviderIsUsed(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void thirdPartySerialInterfaceProviderIsUsed(final GameTestHelper helper) {
        RegistrationTests.thirdPartySerialInterfaceProviderIsUsed(helper);
    }

    // --------------------------------------------------------------------- //

    private RegistrationTestsNeoForge() {
    }
}
