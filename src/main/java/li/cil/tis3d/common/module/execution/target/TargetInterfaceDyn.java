package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.Machine;

/**
 * Interface for the {@link Target#DYN} target.
 * <p>
 * When DYN mode is enabled this target provides read and write on the pipe of
 * the port selected dynamically based on the internal DYN register. This
 * register is set whenever a read or write operation <em>on a real port</em>
 * completes.
 * <p>
 * When DYN mode is disabled this target provides direct access to the DYN
 * register, which accepts and stores a value from 0-4 that encodes the stored
 * port in nil-up-right-down-left positioning. Setting this register to a value
 * outside this range will result in the value being clamped to inside it.
 * <p>
 * Since this is not set upon startup, there needs to be a default behavior,
 * that being it behaves exactly like {@link Target#NIL}.
 */
public final class TargetInterfaceDyn extends AbstractTargetInterfaceSide {

    public TargetInterfaceDyn(final Machine machine, final ModuleExecution module, final Face face) {
        super(machine, module, face);
    }

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
            getState().dyn.ifPresent(port -> beginWrite(port, value));
            return !getState().dyn.isPresent();
    }

    @Override
    public boolean isWriting(){
        return getState().dyn.map(this::isWriting).orElse(false);
    }

    @Override
    public void beginRead() {
            getState().dyn.ifPresent(this::beginRead);
    }

    @Override
    public boolean isReading() {
        return getState().dyn.map(this::isReading).orElse(false);
    }

    @Override
    public boolean canTransfer() {
        return getState().dyn.map(this::canTransfer).orElse(true);
    }

    @Override
    public short read() {
        return getState().dyn.map(this::read).orElse((short) 0);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "DYN [" + getState().dyn.map(Enum::name).orElse("NIL") + "]";
    }
}
