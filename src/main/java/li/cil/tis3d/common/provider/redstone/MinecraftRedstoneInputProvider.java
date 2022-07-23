package li.cil.tis3d.common.provider.redstone;

import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class MinecraftRedstoneInputProvider implements RedstoneInputProvider {
    @Override
    public int getInput(final Level level, final BlockPos position, final Direction face) {
        final BlockPos inputPos = position.relative(face);
        if (!LevelUtils.isLoaded(level, inputPos)) {
            return 0;
        }

        return (short) level.getSignal(inputPos, face);
    }
}
