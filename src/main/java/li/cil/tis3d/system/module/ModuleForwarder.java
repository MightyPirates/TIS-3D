package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.api.prefab.AbstractModule;

/**
 * This is a "virtual" module for internal use, forwarding data on all incoming
 * ports to the linked sink forwarder. This is used to transfer data between
 * two adjacent casings.
 * <p>
 * Forwarders are always created in pairs, and each takes care of one of the two
 * directions data has to be moved.
 */
public final class ModuleForwarder extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Computed data

    private ModuleForwarder other;

    // --------------------------------------------------------------------- //

    public ModuleForwarder(final Casing casing, final Face face) {
        super(casing, face);
    }

    public void setSink(final ModuleForwarder other) {
        this.other = other;
    }

    private void beginForwarding(final Side side) {
        final Port inputPort = getCasing().getInputPort(getFace(), side);
        final Port outputPort = other.getCasing().getOutputPort(other.getFace(), flipSide(side));
        if (outputPort.isReading()) {
            if (!outputPort.isWriting()) {
                if (!inputPort.isReading()) {
                    inputPort.beginRead();
                }
                if (inputPort.isTransferring()) {
                    outputPort.beginWrite(inputPort.read());
                }
            }
        }
    }

    private static Side flipSide(final Side side) {
        return (side == Side.LEFT || side == Side.RIGHT) ? side.getOpposite() : side;
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Side side : Side.VALUES) {
            beginForwarding(side);
        }
    }

    @Override
    public void onWriteComplete(final Side side) {
        beginForwarding(side);
    }
}
