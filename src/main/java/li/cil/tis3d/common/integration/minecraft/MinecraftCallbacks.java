package li.cil.tis3d.common.integration.minecraft;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.Redstone;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

final class MinecraftCallbacks {
    static int getInput(final Redstone module) {
        final Face face = module.getFace();
        final Direction facing = Face.toDirection(face);
        final World world = module.getCasing().getCasingWorld();
        final BlockPos inputPos = module.getCasing().getPosition().offset(facing);
        //~ if (!world.isBlockLoaded(inputPos)) {
            //~ return 0;
        //~ }

        final int input = world.getEmittedRedstonePower(inputPos, facing);
        if (input >= 15) {
            return (short)input;
        } else {
            final BlockState state = world.getBlockState(inputPos);
            return (short)Math.max(input, state.getBlock() == Blocks.REDSTONE_WIRE ? state.get(RedstoneWireBlock.POWER) : 0);
        }
    }

    // --------------------------------------------------------------------- //

    private MinecraftCallbacks() {
    }
}
