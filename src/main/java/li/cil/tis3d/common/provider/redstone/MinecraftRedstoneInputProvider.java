package li.cil.tis3d.common.provider.redstone;

import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinecraftRedstoneInputProvider extends ForgeRegistryEntry<RedstoneInputProvider> implements RedstoneInputProvider {
    @Override
    public int getInput(final World world, final BlockPos pos, final Direction face) {
        final BlockPos inputPos = pos.offset(face);
        if (!WorldUtils.isBlockLoaded(world, inputPos)) {
            return 0;
        }

        final int input = world.getRedstonePower(inputPos, face);
        if (input >= 15) {
            return (short) input;
        } else {
            final BlockState state = world.getBlockState(inputPos);
            return (short) Math.max(input, state.getBlock() == Blocks.REDSTONE_WIRE ? state.get(RedstoneWireBlock.POWER) : 0);
        }
    }
}
