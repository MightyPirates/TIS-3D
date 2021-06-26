package li.cil.tis3d.common.provider.redstone;

import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinecraftRedstoneInputProvider extends ForgeRegistryEntry<RedstoneInputProvider> implements RedstoneInputProvider {
    @Override
    public int getInput(final World world, final BlockPos pos, final Direction face) {
        final BlockPos inputPos = pos.relative(face);
        if (!WorldUtils.isLoaded(world, inputPos)) {
            return 0;
        }

        return (short) world.getSignal(inputPos, face);
    }
}
