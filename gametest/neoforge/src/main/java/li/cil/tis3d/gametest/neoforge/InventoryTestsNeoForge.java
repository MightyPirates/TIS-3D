/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.InventoryTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void hoppersRespectTheCasingLock(final GameTestHelper helper) {
        InventoryTests.hoppersRespectTheCasingLock(helper);
    }

    // --------------------------------------------------------------------- //

    private InventoryTestsNeoForge() {
    }
}
