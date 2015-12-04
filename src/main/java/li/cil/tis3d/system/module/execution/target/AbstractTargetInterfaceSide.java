package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.ModuleExecution;
import li.cil.tis3d.system.module.execution.Machine;

abstract class AbstractTargetInterfaceSide extends AbstractTargetInterface {
    private final ModuleExecution module;
    private final Face face;

    protected AbstractTargetInterfaceSide(final Machine machine, final ModuleExecution module, final Face face) {
        super(machine);
        this.module = module;
        this.face = face;
    }

    // --------------------------------------------------------------------- //

    protected final void beginWrite(final Port port, final int value) {
        getCasing().getSendingPipe(face, getRotatedPort(port)).beginWrite(value);
    }

    protected final void cancelWrite(final Port port) {
        getCasing().getSendingPipe(face, getRotatedPort(port)).cancelWrite();
    }

    protected final boolean isWriting(final Port port) {
        return getCasing().getSendingPipe(face, getRotatedPort(port)).isWriting();
    }

    protected final void beginRead(final Port port) {
        getCasing().getReceivingPipe(face, getRotatedPort(port)).beginRead();
    }

    protected final void cancelRead(final Port port) {
        getCasing().getReceivingPipe(face, getRotatedPort(port)).cancelRead();
    }

    protected final boolean isReading(final Port port) {
        return getCasing().getReceivingPipe(face, getRotatedPort(port)).isReading();
    }

    protected final boolean canTransfer(final Port port) {
        return getCasing().getReceivingPipe(face, getRotatedPort(port)).canTransfer();
    }

    protected final int read(final Port port) {
        return getCasing().getReceivingPipe(face, getRotatedPort(port)).read();
    }

    // --------------------------------------------------------------------- //

    private Casing getCasing() {
        return module.getCasing();
    }

    private Port getRotatedPort(final Port port) {
        return module.getRotatedPort(port);
    }
}
