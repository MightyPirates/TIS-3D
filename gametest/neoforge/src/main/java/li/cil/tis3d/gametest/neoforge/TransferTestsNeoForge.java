/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.TransferTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "transfer")
public final class TransferTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Single casing none.")
    public static void singleCasingNone(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Single casing cw90.")
    public static void singleCasingCw90(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Single casing cw180.")
    public static void singleCasingCw180(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Single casing ccw90.")
    public static void singleCasingCcw90(final GameTestHelper helper) {
        TransferTests.singleCasing(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Planar casings none.")
    public static void planarCasingsNone(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Planar casings cw90.")
    public static void planarCasingsCw90(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Planar casings cw180.")
    public static void planarCasingsCw180(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Planar casings ccw90.")
    public static void planarCasingsCcw90(final GameTestHelper helper) {
        TransferTests.planarCasings(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Corner casings none.")
    public static void cornerCasingsNone(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.NONE, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Corner casings cw90.")
    public static void cornerCasingsCw90(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Corner casings cw180.")
    public static void cornerCasingsCw180(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.CLOCKWISE_180, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Corner casings ccw90.")
    public static void cornerCasingsCcw90(final GameTestHelper helper) {
        TransferTests.cornerCasings(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private TransferTestsNeoForge() {
    }
}
