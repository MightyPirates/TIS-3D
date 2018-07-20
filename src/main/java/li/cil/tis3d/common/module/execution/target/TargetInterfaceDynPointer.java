package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.Machine;

import java.util.Optional;

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
public final class TargetInterfaceDynPointer extends AbstractTargetInterfaceSide {

    public TargetInterfaceDynPointer(final Machine machine, final ModuleExecution module, final Face face) {
        super(machine, module, face);
    }

    // --------------------------------------------------------------------- //
    // TargetInterface

    @Override
    public boolean beginWrite(final short value) {
        if(value < 0)
            getState().dyn = Optional.empty();
        else
            getState().dyn = Optional.of(Port.CLOCKWISE[value % Port.CLOCKWISE.length]);
        return true;
    }

    @Override
    public boolean isWriting(){
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
        return getState().dyn.map(port -> (short) Port.ROTATION[port.ordinal()]).orElse((short)-1);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "DYNP [" + getState().dyn.map((it) -> it.ordinal()).orElse(-1) + "]";
    }
}
