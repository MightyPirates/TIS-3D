/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.TransferTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class TransferTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void singleCasingNone(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void singleCasingCw90(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void singleCasingCw180(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void singleCasingCcw90(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void planarCasingsNone(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void planarCasingsCw90(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void planarCasingsCw180(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void planarCasingsCcw90(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void cornerCasingsNone(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.NONE, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void cornerCasingsCw90(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void cornerCasingsCw180(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.CLOCKWISE_180, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void cornerCasingsCcw90(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private TransferTestsNeoForge() {
    }
}
