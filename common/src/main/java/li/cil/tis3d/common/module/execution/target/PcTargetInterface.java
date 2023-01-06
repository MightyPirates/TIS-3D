package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.common.module.execution.Machine;

/**
 * Interface for the {@link Target#PC} target.
 * <p>
 * Provides instant read and write on the program counter
 */
public final class PcTargetInterface extends AbstractTargetInterface {
    public PcTargetInterface(final Machine machine) {
        super(machine);
    }

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
        getState().pc = value;
        return true;
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
    public short read() {
        return (short) getState().pc;
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "PC";
    }
}
