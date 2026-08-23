/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.ModuleTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class ModuleTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void redstoneModuleExchangesWithWorld(final GameTestHelper helper) {
        ModuleTests.redstoneModuleExchangesWithWorld(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void infraredPacketReachesReceiver(final GameTestHelper helper) {
        ModuleTests.infraredPacketReachesReceiver(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void readOnlyMemoryModuleIgnoresConcurrentWrite(final GameTestHelper helper) {
        ModuleTests.readOnlyMemoryModuleIgnoresConcurrentWrite(helper);
    }

    // --------------------------------------------------------------------- //

    private ModuleTestsNeoForge() {
    }
}
