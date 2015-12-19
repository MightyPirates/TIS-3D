package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Optional;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Module;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

@Optional.InterfaceList({
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledEmitter", modid = ModRedLogic.MOD_ID),
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IBundledUpdatable", modid = ModRedLogic.MOD_ID)
})
public interface RedLogicBundledRedstone extends IBundledEmitter, IBundledUpdatable {
    Casing getCasing();

    @Optional.Method(modid = ModRedLogic.MOD_ID)
    @Override
    default byte[] getBundledCableStrength(final int blockFace, final int toDirection) {
        final Module module = getCasing().getModule(Face.VALUES[toDirection]);
        if (module instanceof BundledRedstone) {
            final BundledRedstone bundledRedstone = (BundledRedstone) module;

            final byte[] signal = new byte[16];
            for (int channel = 0; channel < signal.length; channel++) {
                signal[channel] = (byte) bundledRedstone.getBundledRedstoneOutput(channel);
            }
            return signal;
        }
        return null;
    }

    @Optional.Method(modid = ModRedLogic.MOD_ID)
    @Override
    default void onBundledInputChanged() {
        for (final Face face : Face.VALUES) {
            final EnumFacing facing = Face.toEnumFacing(face);
            final Module module = getCasing().getModule(face);
            if (module instanceof BundledRedstone) {
                final BundledRedstone bundledRedstone = (BundledRedstone) module;

                final World world = getCasing().getCasingWorld();
                final int inputX = getCasing().getPositionX() + facing.getFrontOffsetX();
                final int inputY = getCasing().getPositionY() + facing.getFrontOffsetY();
                final int inputZ = getCasing().getPositionZ() + facing.getFrontOffsetZ();
                if (world.blockExists(inputX, inputY, inputZ)) {
                    final TileEntity tileEntity = world.getTileEntity(inputX, inputY, inputZ);

                    if (tileEntity instanceof IBundledEmitter) {
                        final IBundledEmitter emitter = (IBundledEmitter) tileEntity;

                        final byte[] maxSignal = new byte[16];
                        for (int blockFace = 0; blockFace < 6; blockFace++) {
                            final byte[] signal = emitter.getBundledCableStrength(blockFace, face.getOpposite().ordinal());
                            if (signal != null) {
                                for (int channel = 0; channel < maxSignal.length; channel++) {
                                    if ((signal[channel] & 0xFF) > (maxSignal[channel] & 0xFF)) {
                                        maxSignal[channel] = signal[channel];
                                    }
                                }
                            }
                        }

                        for (int channel = 0; channel < maxSignal.length; channel++) {
                            bundledRedstone.setBundledRedstoneInput(channel, (short) (maxSignal[channel] & 0xFF));
                        }
                    }
                }
            }
        }
    }
}
