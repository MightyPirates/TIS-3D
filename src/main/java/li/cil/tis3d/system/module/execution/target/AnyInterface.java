package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;

public final class AnyInterface extends AbstractSideInterface {
    public AnyInterface(final Machine machine, final Casing casing, final Face face) {
        super(machine, casing, face);
    }

    @Override
    public void beginWrite(final int value) {
        for (final Side side : Side.VALUES) {
            if (!isWriting(side)) {
                beginWrite(side, value);
            }
        }
    }

    @Override
    public void cancelWrite() {
        for (final Side side : Side.VALUES) {
            cancelWrite(side);
        }
    }

    @Override
    public boolean isWriting() {
        return isWriting(Side.LEFT) && isWriting(Side.RIGHT) && isWriting(Side.UP) && isWriting(Side.DOWN);
    }

    @Override
    public boolean isOutputTransferring() {
        return isOutputTransferring(Side.LEFT) || isOutputTransferring(Side.RIGHT) || isOutputTransferring(Side.UP) || isOutputTransferring(Side.DOWN);
    }

    @Override
    public void beginRead() {
        for (final Side side : Side.VALUES) {
            if (!isReading(side)) {
                beginRead(side);
            }
        }
    }

    @Override
    public boolean isReading() {
        return isReading(Side.LEFT) && isReading(Side.RIGHT) && isReading(Side.UP) && isReading(Side.DOWN);
    }

    @Override
    public boolean isInputTransferring() {
        return isInputTransferring(Side.LEFT) || isInputTransferring(Side.RIGHT) || isInputTransferring(Side.UP) || isInputTransferring(Side.DOWN);
    }

    @Override
    public int read() {
        if (isInputTransferring(Side.LEFT)) {
            cancelRead(Side.RIGHT);
            cancelRead(Side.UP);
            cancelRead(Side.DOWN);
            return read(Side.LEFT);
        } else cancelRead(Side.LEFT);
        if (isInputTransferring(Side.RIGHT)) {
            cancelRead(Side.UP);
            cancelRead(Side.DOWN);
            return read(Side.RIGHT);
        } else cancelRead(Side.RIGHT);
        if (isInputTransferring(Side.UP)) {
            cancelRead(Side.DOWN);
            return read(Side.UP);
        } else cancelRead(Side.UP);
        if (isInputTransferring(Side.DOWN)) {
            return read(Side.DOWN);
        } else cancelRead(Side.DOWN);
        throw new IllegalStateException("No data to read. Check beginRead().");
    }
}
