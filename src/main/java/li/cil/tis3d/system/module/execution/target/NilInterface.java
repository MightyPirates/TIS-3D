package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.system.module.execution.Machine;

public final class NilInterface extends AbstractTargetInterface {
    public NilInterface(final Machine machine) {
        super(machine);
    }

    @Override
    public void beginWrite(final int value) {
    }

    @Override
    public void cancelWrite() {
    }

    @Override
    public boolean isWriting() {
        return false;
    }

    @Override
    public boolean isOutputTransferring() {
        return true;
    }

    @Override
    public void beginRead() {
    }

    @Override
    public boolean isReading() {
        return false;
    }

    @Override
    public boolean isInputTransferring() {
        return true;
    }

    @Override
    public int read() {
        return 0;
    }
}
