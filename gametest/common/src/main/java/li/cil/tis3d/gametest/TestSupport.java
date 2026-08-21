/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;


public final class TestSupport {
    public static final String MOD_ID = "tis3d_gametest";
    public static final String TEMPLATE = "empty";

    public static final int WORK_Y = 2;

    public static final BlockPos WIRE_SOURCE_POS = new BlockPos(1, WORK_Y, 2);
    public static final BlockPos[] WIRE_POSITIONS = {
        new BlockPos(2, WORK_Y, 2),
        new BlockPos(3, WORK_Y, 2),
        new BlockPos(4, WORK_Y, 2),
    };
    public static final BlockPos CONTROLLER_POS = new BlockPos(5, WORK_Y, 2);
    public static final BlockPos LEVER_POS = CONTROLLER_POS.above();
    public static final BlockPos CASING_POS = new BlockPos(6, WORK_Y, 2);
    public static final BlockPos CASING_2_POS = new BlockPos(7, WORK_Y, 2);
    public static final BlockPos CONTROLLER_2_POS = CASING_2_POS;
    public static final BlockPos CASING_NEIGHBOR_POS = CASING_POS.above();

    public static final int TOO_MANY_CASINGS = 17;

    // --------------------------------------------------------------------- //

    public static BlockPos casingRunPos(final int index) {
        return CASING_POS.offset(index, 0, 0);
    }

    public static GameTestAssertException failure(final GameTestHelper helper, final String message) {
        return new GameTestAssertException(message);
    }

    public static void assertTrue(final GameTestHelper helper, final String what, final boolean condition) {
        if (!condition) {
            throw failure(helper, what);
        }
    }

    public static void assertEquals(final GameTestHelper helper, final String what, final long expected, final long actual) {
        if (expected != actual) {
            throw failure(helper, what + ": expected " + expected + ", got " + actual);
        }
    }

    public static <T> T requireBlockEntity(final GameTestHelper helper, final BlockPos pos, final Class<T> type) {
        final Object blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        if (!type.isInstance(blockEntity)) {
            throw new GameTestAssertException("no " + type.getSimpleName() + " at " + pos + ", found " + blockEntity);
        }
        return type.cast(blockEntity);
    }

    // --------------------------------------------------------------------- //

    private TestSupport() {
    }
}
