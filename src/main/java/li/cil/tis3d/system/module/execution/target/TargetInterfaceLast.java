package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.system.module.execution.Machine;

public final class TargetInterfaceLast extends TargetInterfaceAbstractSide {
    public TargetInterfaceLast(final Machine machine, final Casing casing, final Face face) {
        super(machine, casing, face);
    }

    @Override
    public boolean beginWrite(final int value) {
        getState().last.ifPresent(port -> beginWrite(port, value));
        return !getState().last.isPresent();
    }

    @Override
    public void cancelWrite() {
        getState().last.ifPresent(this::cancelWrite);
    }

    @Override
    public boolean isWriting() {
        return getState().last.map(this::isWriting).orElse(false);
    }

    @Override
    public void beginRead() {
        getState().last.ifPresent(this::beginRead);
    }

    @Override
    public boolean isReading() {
        return getState().last.map(this::isReading).orElse(false);
    }

    @Override
    public boolean canRead() {
        return getState().last.map(this::canRead).orElse(true);
    }

    @Override
    public int read() {
        return getState().last.map(this::read).orElse(0);
    }
}
