/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.InventoryTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "inventory")
public final class InventoryTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 300)
    @TestHolder(description = "Hoppers respect the casing lock.")
    public static void hoppersRespectTheCasingLock(final GameTestHelper helper) {
        InventoryTests.hoppersRespectTheCasingLock(helper);
    }

    // --------------------------------------------------------------------- //

    private InventoryTestsNeoForge() {
    }
}
