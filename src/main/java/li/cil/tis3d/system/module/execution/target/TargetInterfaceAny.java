package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;

public final class TargetInterfaceAny extends TargetInterfaceAbstractSide {
    public TargetInterfaceAny(final Machine machine, final Casing casing, final Face face) {
        super(machine, casing, face);
    }

    @Override
    public boolean beginWrite(final int value) {
        for (final Port port : Port.VALUES) {
            if (!isWriting(port)) {
                beginWrite(port, value);
            }
        }
        return false;
    }

    @Override
    public void cancelWrite() {
        for (final Port port : Port.VALUES) {
            cancelWrite(port);
        }
    }

    @Override
    public boolean isWriting() {
        return isWriting(Port.LEFT) && isWriting(Port.RIGHT) && isWriting(Port.UP) && isWriting(Port.DOWN);
    }

    @Override
    public void beginRead() {
        for (final Port port : Port.VALUES) {
            if (!isReading(port)) {
                beginRead(port);
            }
        }
    }

    @Override
    public boolean isReading() {
        return isReading(Port.LEFT) && isReading(Port.RIGHT) && isReading(Port.UP) && isReading(Port.DOWN);
    }

    @Override
    public boolean canRead() {
        return canRead(Port.LEFT) || canRead(Port.RIGHT) || canRead(Port.UP) || canRead(Port.DOWN);
    }

    @Override
    public int read() {
        if (canRead(Port.LEFT)) {
            cancelRead(Port.RIGHT);
            cancelRead(Port.UP);
            cancelRead(Port.DOWN);
            return read(Port.LEFT);
        } else cancelRead(Port.LEFT);
        if (canRead(Port.RIGHT)) {
            cancelRead(Port.UP);
            cancelRead(Port.DOWN);
            return read(Port.RIGHT);
        } else cancelRead(Port.RIGHT);
        if (canRead(Port.UP)) {
            cancelRead(Port.DOWN);
            return read(Port.UP);
        } else cancelRead(Port.UP);
        if (canRead(Port.DOWN)) {
            return read(Port.DOWN);
        } else cancelRead(Port.DOWN);
        throw new IllegalStateException("No data to read. Check beginRead().");
    }
}
