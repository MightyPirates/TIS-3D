package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;

public final class SideInterface extends AbstractSideInterface {
    private final Side side;

    public SideInterface(final Machine machine, final Casing casing, final Face face, final Side side) {
        super(machine, casing, face);
        this.side = side;
    }

    @Override
    public void beginWrite(final int value) {
        beginWrite(side, value);
    }

    @Override
    public void cancelWrite() {
        cancelWrite(side);
    }

    @Override
    public boolean isWriting() {
        return isWriting(side);
    }

    @Override
    public boolean isOutputTransferring() {
        return isOutputTransferring(side);
    }

    @Override
    public void beginRead() {
        beginRead(side);
    }

    @Override
    public boolean isReading() {
        return isReading(side);
    }

    @Override
    public boolean isInputTransferring() {
        return isInputTransferring(side);
    }

    @Override
    public int read() {
        return read(side);
    }
}
