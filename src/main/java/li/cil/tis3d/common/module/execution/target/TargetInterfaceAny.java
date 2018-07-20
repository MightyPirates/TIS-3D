package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.Machine;

import java.util.Optional;

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

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
        for (final Port port : Port.VALUES) {
            if (!isWriting(port)) {
                beginWrite(port, value);
            }
        }
        return false;
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
    public short read() {
        for (final Port port : Port.VALUES) {
            if (canTransfer(port)) {
                for (final Port otherPort : Port.VALUES) {
                    if (otherPort != port) {
                        cancelRead(otherPort);
                    }
                }

                getMachine().getState().dyn = Optional.of(port);

                return read(port);
            }
        }

        throw new IllegalStateException("No data to read. Check canTransfer().");
    }

    @Override
    public void onWriteComplete(final Port port) {
        cancelWrite();
        getMachine().getState().dyn = Optional.of(port);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "ANY";
    }

    // --------------------------------------------------------------------- //

    public void cancelWrite() {
        for (final Port port : Port.VALUES) {
            cancelWrite(port);
        }
    }
}
