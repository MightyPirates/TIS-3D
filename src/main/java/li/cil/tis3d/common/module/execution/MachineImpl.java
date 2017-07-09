package li.cil.tis3d.common.module.execution;

import com.google.common.collect.ImmutableMap;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceAcc;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceAny;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceBak;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceLast;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceNil;
import li.cil.tis3d.common.module.execution.target.TargetInterfaceSide;

import java.util.Map;

/**
 * Track machine state and ease communicating with ports of an execution module.
 */
public final class MachineImpl implements Machine {
    // --------------------------------------------------------------------- //
    // Persisted data
    private final MachineState state;

    // --------------------------------------------------------------------- //
    // Computed data

    private final ModuleExecution module;
    private final Map<Target, TargetInterface> interfaces;

    // --------------------------------------------------------------------- //

    public MachineImpl(final ModuleExecution module, final Face face) {
        this.state = new MachineState();
        this.module = module;
        this.interfaces = ImmutableMap.<Target, TargetInterface>builder().
                put(Target.ACC, new TargetInterfaceAcc(this)).
                put(Target.BAK, new TargetInterfaceBak(this)).
                put(Target.NIL, new TargetInterfaceNil(this)).
                put(Target.LEFT, new TargetInterfaceSide(this, module, face, Port.LEFT)).
                put(Target.RIGHT, new TargetInterfaceSide(this, module, face, Port.RIGHT)).
                put(Target.UP, new TargetInterfaceSide(this, module, face, Port.UP)).
                put(Target.DOWN, new TargetInterfaceSide(this, module, face, Port.DOWN)).
                put(Target.ANY, new TargetInterfaceAny(this, module, face)).
                put(Target.LAST, new TargetInterfaceLast(this, module, face)).
                build();
    }

    /**
     * Advance the virtual machine, ensures the machine's state is valid after
     * the instruction finishes.
     *
     * @return <tt>true</tt> if the current instruction changed (even if it's the same again).
     */
    public boolean step() {
        final int pc = state.pc;
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.step(this);
        }

        final boolean stateChanged = state.pc != pc;

        state.validate();

        return stateChanged;
    }

    /**
     * Inform the active instruction that a write operation was completed.
     * <p>
     * Instructions are expected to wait / loop until a write operation they
     * initiated has been completed.
     *
     * @param port the port on which the write operation was completed.
     */
    public void onWriteCompleted(final Port port) {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.onWriteCompleted(this, port);
        }
    }

    /**
     * Utility method for safely retrieving the current instruction.
     *
     * @return the currently active instruction, or <tt>null</tt>.
     */
    private Instruction getInstruction() {
        if (state.pc >= 0 && state.pc < state.instructions.size()) {
            return state.instructions.get(state.pc);
        }
        return null;
    }

    // --------------------------------------------------------------------- //
    // Machine

    @Override
    public MachineState getState() {
        return state;
    }

    @Override
    public TargetInterface getInterface(final Target target) {
        final Target rotatedTarget = getRotatedTarget(target);
        return interfaces.get(rotatedTarget);
    }

    // --------------------------------------------------------------------- //

    /**
     * Adjust the specified target based on the execution module's rotation.
     * <p>
     * Will only do something for port targets.
     *
     * @param target the target to transform.
     * @return the adjusted target.
     */
    private Target getRotatedTarget(final Target target) {
        switch (target) {
            case LEFT:
            case RIGHT:
            case UP:
            case DOWN:
                final int rotation = Port.ROTATION[module.getFacing().ordinal()];
                final Port port = Target.toPort(target);
                final Port rotatedPort = port.rotated(rotation);
                return Target.fromPort(rotatedPort);
        }
        return target;
    }
}
