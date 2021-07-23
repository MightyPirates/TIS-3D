package li.cil.tis3d.common.provider.redstone;

import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinecraftRedstoneInputProvider extends ForgeRegistryEntry<RedstoneInputProvider> implements RedstoneInputProvider {
    @Override
    public int getInput(final Level world, final BlockPos pos, final Direction face) {
        final BlockPos inputPos = pos.relative(face);
        if (!WorldUtils.isLoaded(world, inputPos)) {
            return 0;
        }

        return (short) world.getSignal(inputPos, face);
    }
}
