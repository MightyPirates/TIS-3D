package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.Machine;

/**
 * Interface for the {@link Target#LEFT}, {@link Target#RIGHT},
 * {@link Target#UP} and {@link Target#DOWN} targets.
 * <p>
 * Provides read and write on the pipe of the respective port.
 */
public final class TargetInterfaceSide extends AbstractTargetInterfaceSide {
    private final Port port;

    public TargetInterfaceSide(final Machine machine, final ModuleExecution module, final Face face, final Port port) {
        super(machine, module, face);
        this.port = port;
    }

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
        beginWrite(port, value);
        return false;
    }

    @Override
    public boolean isWriting() {
        return isWriting(port);
    }

    @Override
    public void beginRead() {
        beginRead(port);
    }

    @Override
    public boolean isReading() {
        return isReading(port);
    }

    @Override
    public boolean canTransfer() {
        return canTransfer(port);
    }

    @Override
    public short read() {
        return read(port);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return port.name();
    }
}
