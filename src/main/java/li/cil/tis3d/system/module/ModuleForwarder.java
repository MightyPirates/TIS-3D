package li.cil.tis3d.system.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
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

    private void beginForwarding(final Port port) {
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        final Pipe sendingPipe = other.getCasing().getSendingPipe(other.getFace(), flipSide(port));
        if (!receivingPipe.isReading()) {
            receivingPipe.beginRead();
        }
        if (sendingPipe.isReading() && !sendingPipe.isWriting() && receivingPipe.canTransfer()) {
            sendingPipe.beginWrite(receivingPipe.read());
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
}
