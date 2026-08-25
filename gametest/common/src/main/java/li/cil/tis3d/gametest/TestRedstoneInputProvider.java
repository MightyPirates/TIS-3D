/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.module.RedstoneInputProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class TestRedstoneInputProvider implements RedstoneInputProvider {
    private static final Map<Key, Integer> SIGNALS = new HashMap<>();

    private record Key(BlockPos position, Direction side) {
    }

    // --------------------------------------------------------------------- //

    public static void setSignal(final BlockPos position, final Direction side, final int signal) {
        SIGNALS.put(new Key(position.immutable(), side), signal);
    }

    public static void reset() {
        SIGNALS.clear();
    }
    // --------------------------------------------------------------------- //

    @Override
    public int getInput(final Level level, final BlockPos position, final Direction side) {
        return SIGNALS.getOrDefault(new Key(position.immutable(), side), 0);
    }
}
