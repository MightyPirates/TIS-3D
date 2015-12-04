package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.ModuleExecution;
import li.cil.tis3d.system.module.execution.Machine;

/**
 * Interface for the {@link Target#ANY} target.
 * <p>
 * Provides read and write access to all {@link Port}s on the module in a
 * <em>simultaneous</em> fashion. If any port finishes its transfer, all other
 * ports will also be reset.
 */
public final class TargetInterfaceAny extends AbstractTargetInterfaceSide {
    public TargetInterfaceAny(final Machine machine, final ModuleExecution module, final Face face) {
        super(machine, module, face);
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
    public boolean canTransfer() {
        return canTransfer(Port.LEFT) || canTransfer(Port.RIGHT) || canTransfer(Port.UP) || canTransfer(Port.DOWN);
    }

    @Override
    public int read() {
        if (canTransfer(Port.LEFT)) {
            cancelRead(Port.RIGHT);
            cancelRead(Port.UP);
            cancelRead(Port.DOWN);
            return read(Port.LEFT);
        } else cancelRead(Port.LEFT);
        if (canTransfer(Port.RIGHT)) {
            cancelRead(Port.UP);
            cancelRead(Port.DOWN);
            return read(Port.RIGHT);
        } else cancelRead(Port.RIGHT);
        if (canTransfer(Port.UP)) {
            cancelRead(Port.DOWN);
            return read(Port.UP);
        } else cancelRead(Port.UP);
        if (canTransfer(Port.DOWN)) {
            return read(Port.DOWN);
        } else cancelRead(Port.DOWN);
        throw new IllegalStateException("No data to read. Check canTransfer().");
    }

    @Override
    public void onWriteComplete(final Machine machine, final Port port) {
        cancelWrite();
    }
}
