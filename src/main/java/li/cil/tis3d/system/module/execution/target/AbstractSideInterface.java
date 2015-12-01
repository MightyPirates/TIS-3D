package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;

abstract class AbstractSideInterface extends AbstractTargetInterface {
    private final Casing casing;
    private final Face face;

    protected AbstractSideInterface(final Machine machine, final Casing casing, final Face face) {
        super(machine);
        this.casing = casing;
        this.face = face;
    }

    protected final void beginWrite(final Side side, final int value) {
        casing.getOutputPort(face, side).beginWrite(value);
    }

    protected final void cancelWrite(final Side side) {
        casing.getOutputPort(face, side).cancelWrite();
    }

    protected final boolean isWriting(final Side side) {
        return casing.getOutputPort(face, side).isWriting();
    }

    protected final boolean isOutputTransferring(final Side side) {
        return casing.getOutputPort(face, side).isTransferring();
    }

    protected final void beginRead(final Side side) {
        casing.getInputPort(face, side).beginRead();
    }

    protected final boolean isReading(final Side side) {
        return casing.getInputPort(face, side).isReading();
    }

    protected final boolean isInputTransferring(final Side side) {
        return casing.getInputPort(face, side).isTransferring();
    }

    protected final int read(final Side side) {
        return casing.getInputPort(face, side).read();
    }

    protected final void cancelRead(final Side side) {
        casing.getInputPort(face, side).cancelRead();
    }
}
