/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.RecipeTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "recipe")
public final class RecipeTestsNeoForge {
    @GameTest(template = TEMPLATE_ID)
    @TestHolder(description = "Every mod item is craftable.")
    public static void everyModItemIsCraftable(final GameTestHelper helper) {
        RecipeTests.everyModItemIsCraftable(helper);
    }

    @GameTest(template = TEMPLATE_ID)
    @TestHolder(description = "Every recipe crafts in crafting table.")
    public static void everyRecipeCraftsInCraftingTable(final GameTestHelper helper) {
        RecipeTests.everyRecipeCraftsInCraftingTable(helper);
    }

    // --------------------------------------------------------------------- //

    private RecipeTestsNeoForge() {
    }
}
