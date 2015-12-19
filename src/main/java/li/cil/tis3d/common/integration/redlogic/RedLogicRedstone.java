package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Optional;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import mods.immibis.redlogic.api.wiring.IRedstoneUpdatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

@Optional.InterfaceList({
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneEmitter", modid = ModRedLogic.MOD_ID),
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneUpdatable", modid = ModRedLogic.MOD_ID)
})
public interface RedLogicRedstone extends IRedstoneEmitter, IRedstoneUpdatable {
    Casing getCasing();

    @Optional.Method(modid = ModRedLogic.MOD_ID)
    @Override
    default short getEmittedSignalStrength(final int blockFace, final int toDirection) {
        final Module module = getCasing().getModule(Face.VALUES[toDirection]);
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            return redstone.getRedstoneOutput();
        }
        return 0;
    }

    @Optional.Method(modid = ModRedLogic.MOD_ID)
    @Override
    default void onRedstoneInputChanged() {
        for (final Face face : Face.VALUES) {
            final EnumFacing facing = Face.toEnumFacing(face);
            final Module module = getCasing().getModule(face);
            if (module instanceof Redstone) {
                final Redstone redstone = (Redstone) module;

                final World world = getCasing().getCasingWorld();
                final int inputX = getCasing().getPositionX() + facing.getFrontOffsetX();
                final int inputY = getCasing().getPositionY() + facing.getFrontOffsetY();
                final int inputZ = getCasing().getPositionZ() + facing.getFrontOffsetZ();
                if (world.blockExists(inputX, inputY, inputZ)) {
                    final TileEntity tileEntity = world.getTileEntity(inputX, inputY, inputZ);

                    if (tileEntity instanceof IRedstoneEmitter) {
                        final IRedstoneEmitter emitter = (IRedstoneEmitter) tileEntity;

                        short maxSignal = 0;
                        for (int blockFace = 0; blockFace < 6; blockFace++) {
                            final short signal = emitter.getEmittedSignalStrength(blockFace, face.getOpposite().ordinal());
                            if ((signal & 0xFFFF) > (maxSignal & 0xFFFF)) {
                                maxSignal = (short) (signal & 0xFFFF);
                            }
                        }
                        redstone.setRedstoneInput(maxSignal);
                    }
                }
            }
        }
    }
}
