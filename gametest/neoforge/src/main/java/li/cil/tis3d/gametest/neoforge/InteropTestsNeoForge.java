/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.InteropTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class InteropTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneReadsFromWorldNone(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneReadsFromWorldCw90(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneReadsFromWorldCw180(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneReadsFromWorldCcw90(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneWritesToWorldNone(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneWritesToWorldCw90(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneWritesToWorldCw180(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void redstoneWritesToWorldCcw90(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void serialPortExchangesWithWorldNone(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void serialPortExchangesWithWorldCw90(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void serialPortExchangesWithWorldCw180(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void serialPortExchangesWithWorldCcw90(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredSendsIntoWorldNone(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredSendsIntoWorldCw90(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredSendsIntoWorldCw180(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredSendsIntoWorldCcw90(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredReceivesFromWorldNone(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredReceivesFromWorldCw90(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredReceivesFromWorldCw180(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void infraredReceivesFromWorldCcw90(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private InteropTestsNeoForge() {
    }
}
