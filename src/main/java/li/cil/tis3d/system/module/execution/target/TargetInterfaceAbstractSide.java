package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;

abstract class TargetInterfaceAbstractSide extends AbstractTargetInterface {
    private final Casing casing;
    private final Face face;

    protected TargetInterfaceAbstractSide(final Machine machine, final Casing casing, final Face face) {
        super(machine);
        this.casing = casing;
        this.face = face;
    }

    protected final void beginWrite(final Port port, final int value) {
        casing.getSendingPipe(face, port).beginWrite(value);
    }

    protected final void cancelWrite(final Port port) {
        casing.getSendingPipe(face, port).cancelWrite();
    }

    protected final boolean isWriting(final Port port) {
        return casing.getSendingPipe(face, port).isWriting();
    }

    protected final boolean canWrite(final Port port) {
        return casing.getSendingPipe(face, port).canTransfer();
    }

    protected final void beginRead(final Port port) {
        casing.getReceivingPipe(face, port).beginRead();
    }

    protected final void cancelRead(final Port port) {
        casing.getReceivingPipe(face, port).cancelRead();
    }

    protected final boolean isReading(final Port port) {
        return casing.getReceivingPipe(face, port).isReading();
    }

    protected final boolean canRead(final Port port) {
        return casing.getReceivingPipe(face, port).canTransfer();
    }

    protected final int read(final Port port) {
        return casing.getReceivingPipe(face, port).read();
    }
}
