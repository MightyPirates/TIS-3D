package li.cil.tis3d.system.module.execution.target;

import li.cil.tis3d.system.module.execution.Machine;

public final class BakInterface extends AbstractTargetInterface {
    public BakInterface(final Machine machine) {
        super(machine);
    }

    @Override
    public void beginWrite(final int value) {
        throw throwOnWrite();
    }

    @Override
    public void cancelWrite() {
        throw throwOnWrite();
    }

    @Override
    public boolean isWriting() {
        throw throwOnWrite();
    }

    @Override
    public boolean isOutputTransferring() {
        throw throwOnWrite();
    }

    @Override
    public void beginRead() {
        throw throwOnRead();
    }

    @Override
    public boolean isReading() {
        throw throwOnRead();
    }

    @Override
    public boolean isInputTransferring() {
        throw throwOnRead();
    }

    @Override
    public int read() {
        throw throwOnRead();
    }

    private static IllegalArgumentException throwOnWrite() {
        throw new IllegalStateException("BAK cannot be written to directly.");
    }

    private static IllegalArgumentException throwOnRead() {
        throw new IllegalStateException("BAK cannot be read from directly.");
    }
}
