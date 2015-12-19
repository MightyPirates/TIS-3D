package li.cil.tis3d.common.integration.vanilla;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.common.integration.redstone.RedstoneInputProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class RedstoneInputProviderVanilla implements RedstoneInputProvider {
    @Override
    public int getInput(final Redstone module) {
        final Face face = module.getFace();
        final EnumFacing facing = Face.toEnumFacing(face);
        final World world = module.getCasing().getCasingWorld();
        final int inputX = module.getCasing().getPositionX() + facing.getFrontOffsetX();
        final int inputY = module.getCasing().getPositionY() + facing.getFrontOffsetY();
        final int inputZ = module.getCasing().getPositionZ() + facing.getFrontOffsetZ();
        if (!world.blockExists(inputX, inputY, inputZ)) {
            return 0;
        }

        final int input = world.isBlockProvidingPowerTo(inputX, inputY, inputZ, facing.ordinal());
        if (input >= 15) {
            return (short) input;
        } else {
            final Block block = world.getBlock(inputX, inputY, inputZ);
            return (short) Math.max(input, block == Blocks.redstone_wire ? world.getBlockMetadata(inputX, inputY, inputZ) : 0);
        }
    }
}
