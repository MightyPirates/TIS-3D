/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.fabric;

import li.cil.tis3d.gametest.RecipeTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class RecipeTestsFabric {
    @GameTest
    public void everyModItemIsCraftable(final GameTestHelper helper) {
        RecipeTests.everyModItemIsCraftable(helper);
    }

    @GameTest
    public void everyRecipeCraftsInCraftingTable(final GameTestHelper helper) {
        RecipeTests.everyRecipeCraftsInCraftingTable(helper);
    }
}
