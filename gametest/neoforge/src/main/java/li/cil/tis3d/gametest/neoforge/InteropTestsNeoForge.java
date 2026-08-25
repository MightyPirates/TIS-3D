/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.InteropTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "interop")
public final class InteropTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone reads from world none.")
    public static void redstoneReadsFromWorldNone(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone reads from world cw90.")
    public static void redstoneReadsFromWorldCw90(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone reads from world cw180.")
    public static void redstoneReadsFromWorldCw180(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone reads from world ccw90.")
    public static void redstoneReadsFromWorldCcw90(final GameTestHelper helper) {
        InteropTests.redstoneReadsFromWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone writes to world none.")
    public static void redstoneWritesToWorldNone(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone writes to world cw90.")
    public static void redstoneWritesToWorldCw90(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone writes to world cw180.")
    public static void redstoneWritesToWorldCw180(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Redstone writes to world ccw90.")
    public static void redstoneWritesToWorldCcw90(final GameTestHelper helper) {
        InteropTests.redstoneWritesToWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Serial port exchanges with world none.")
    public static void serialPortExchangesWithWorldNone(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Serial port exchanges with world cw90.")
    public static void serialPortExchangesWithWorldCw90(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Serial port exchanges with world cw180.")
    public static void serialPortExchangesWithWorldCw180(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Serial port exchanges with world ccw90.")
    public static void serialPortExchangesWithWorldCcw90(final GameTestHelper helper) {
        InteropTests.serialPortExchangesWithWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared sends into world none.")
    public static void infraredSendsIntoWorldNone(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared sends into world cw90.")
    public static void infraredSendsIntoWorldCw90(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared sends into world cw180.")
    public static void infraredSendsIntoWorldCw180(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared sends into world ccw90.")
    public static void infraredSendsIntoWorldCcw90(final GameTestHelper helper) {
        InteropTests.infraredSendsIntoWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared receives from world none.")
    public static void infraredReceivesFromWorldNone(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared receives from world cw90.")
    public static void infraredReceivesFromWorldCw90(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared receives from world cw180.")
    public static void infraredReceivesFromWorldCw180(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 400)
    @TestHolder(description = "Infrared receives from world ccw90.")
    public static void infraredReceivesFromWorldCcw90(final GameTestHelper helper) {
        InteropTests.infraredReceivesFromWorld(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private InteropTestsNeoForge() {
    }
}
