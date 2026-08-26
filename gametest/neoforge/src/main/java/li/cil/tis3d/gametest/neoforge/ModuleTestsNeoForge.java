/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.ModuleTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "module")
public final class ModuleTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Redstone module exchanges with world.")
    public static void redstoneModuleExchangesWithWorld(final GameTestHelper helper) {
        ModuleTests.redstoneModuleExchangesWithWorld(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Infrared packet reaches receiver.")
    public static void infraredPacketReachesReceiver(final GameTestHelper helper) {
        ModuleTests.infraredPacketReachesReceiver(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Read only memory module ignores concurrent write.")
    public static void readOnlyMemoryModuleIgnoresConcurrentWrite(final GameTestHelper helper) {
        ModuleTests.readOnlyMemoryModuleIgnoresConcurrentWrite(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on y pos.")
    public static void anyWriteUsesFirstPortOnYPos(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnYPos(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on y neg.")
    public static void anyWriteUsesFirstPortOnYNeg(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnYNeg(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on x pos.")
    public static void anyWriteUsesFirstPortOnXPos(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnXPos(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on x neg.")
    public static void anyWriteUsesFirstPortOnXNeg(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnXNeg(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on z pos.")
    public static void anyWriteUsesFirstPortOnZPos(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnZPos(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write uses first port on z neg.")
    public static void anyWriteUsesFirstPortOnZNeg(final GameTestHelper helper) {
        ModuleTests.anyWriteUsesFirstPortOnZNeg(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write is not pinned to full queue.")
    public static void anyWriteIsNotPinnedToFullQueue(final GameTestHelper helper) {
        ModuleTests.anyWriteIsNotPinnedToFullQueue(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Any write survives reload.")
    public static void anyWriteSurvivesReload(final GameTestHelper helper) {
        ModuleTests.anyWriteSurvivesReload(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Full queue withdraws its reads.")
    public static void fullQueueWithdrawsItsReads(final GameTestHelper helper) {
        ModuleTests.fullQueueWithdrawsItsReads(helper);
    }

    // --------------------------------------------------------------------- //

    private ModuleTestsNeoForge() {
    }
}
