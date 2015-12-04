package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.ModuleExecution;
import li.cil.tis3d.system.module.execution.Machine;

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

    @Override
    public boolean beginWrite(final int value) {
        beginWrite(port, value);
        return false;
    }

    @Override
    public void cancelWrite() {
        cancelWrite(port);
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
    public int read() {
        return read(port);
    }
}
