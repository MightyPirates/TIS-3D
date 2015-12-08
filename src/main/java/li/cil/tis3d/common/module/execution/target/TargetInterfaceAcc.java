package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.common.module.execution.Machine;

/**
 * Interface for the {@link Target#ACC} target.
 * <p>
 * Provides instant read and write on the accumulator register.
 */
public final class TargetInterfaceAcc extends AbstractTargetInterface {
    public TargetInterfaceAcc(final Machine machine) {
        super(machine);
    }

    @Override
    public boolean beginWrite(final int value) {
        getState().acc = value;
        return true;
    }

    @Override
    public void cancelWrite() {
    }

    @Override
    public boolean isWriting() {
        return false;
    }

    @Override
    public void beginRead() {
    }

    @Override
    public boolean isReading() {
        return false;
    }

    @Override
    public boolean canTransfer() {
        return true;
    }

    @Override
    public int read() {
        return getState().acc;
    }
}
