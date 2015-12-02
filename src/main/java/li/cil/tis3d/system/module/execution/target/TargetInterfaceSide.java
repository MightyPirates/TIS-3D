package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;

public final class TargetInterfaceSide extends TargetInterfaceAbstractSide {
    private final Port port;

    public TargetInterfaceSide(final Machine machine, final Casing casing, final Face face, final Port port) {
        super(machine, casing, face);
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
    public boolean canRead() {
        return canRead(port);
    }

    @Override
    public int read() {
        return read(port);
    }
}
