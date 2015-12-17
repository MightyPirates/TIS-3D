package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;

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

    private void beginForwarding(final Port port) {
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        final Pipe sendingPipe = other.getCasing().getSendingPipe(other.getFace(), flipSide(port));
        if (sendingPipe.isReading() && !sendingPipe.isWriting()) {
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                sendingPipe.beginWrite(receivingPipe.read());
            }
        } else if (receivingPipe.isReading()) {
            receivingPipe.cancelRead();
        }
    }

    private static Port flipSide(final Port port) {
        return (port == Port.LEFT || port == Port.RIGHT) ? port.getOpposite() : port;
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Port port : Port.VALUES) {
            beginForwarding(port);
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        beginForwarding(port);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "{" + getCasing().getPositionX() + ", " + getCasing().getPositionY() + ", " + getCasing().getPositionZ() + "}: " + getFace().toString();
    }
}
