package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.Machine;

/**
 * Interface for the {@link Target#LAST} target.
 * <p>
 * Provides read and write on the pipe of the port that was last read from
 * or written to. This is set whenever a read or write operation <em>on a
 * real port</em> completes.
 * <p>
 * Since this is not set upon startup, there needs to be a default behavior,
 * that being it behaves exactly like {@link Target#NIL}.
 */
public final class TargetInterfaceLast extends AbstractTargetInterfaceSide {
    public TargetInterfaceLast(final Machine machine, final ModuleExecution module, final Face face) {
        super(machine, module, face);
    }

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
        getState().last.ifPresent(port -> beginWrite(port, value));
        return !getState().last.isPresent();
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
    public boolean canTransfer() {
        return getState().last.map(this::canTransfer).orElse(true);
    }

    @Override
    public short read() {
        return getState().last.map(this::read).orElse((short) 0);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "LAST [" + getState().last.map(Enum::name).orElse("NIL") + "]";
    }
}
